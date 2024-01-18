package jimuanco.jimslog.api.service.post;

import jakarta.persistence.EntityManager;
import jimuanco.jimslog.api.service.post.request.PostCreateServiceRequest;
import jimuanco.jimslog.api.service.post.request.PostEditServiceRequest;
import jimuanco.jimslog.api.service.post.request.PostSearchServiceRequest;
import jimuanco.jimslog.api.service.post.response.PostResponse;
import jimuanco.jimslog.domain.menu.Menu;
import jimuanco.jimslog.domain.menu.MenuRepository;
import jimuanco.jimslog.domain.post.Post;
import jimuanco.jimslog.domain.post.PostRepository;
import jimuanco.jimslog.exception.MenuNotFound;
import jimuanco.jimslog.exception.PostNotFound;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Transactional
@SpringBootTest
class PostServiceTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MenuRepository menuRepository;

    @DisplayName("새로운 글을 등록할때 munuId를 넣지 않으면 메뉴가 지정되지 않는다.")
    @Test
    void createPostWithoutMenuId() {
        // given
        PostCreateServiceRequest request = PostCreateServiceRequest.builder()
                .title("글제목 입니다.")
                .content("글내용 입니다.")
                .build();

        // when
        postService.createPost(request);
        
        // then
        List<Post> posts = postRepository.findAll();
        assertThat(posts).hasSize(1)
                .extracting("title", "content", "menu")
                .contains(
                        tuple("글제목 입니다.", "글내용 입니다.", null)
                );
    }

    @DisplayName("새로운 글을 등록할때 munuId를 넣으면 메뉴가 지정된다.")
    @Test
    void createPostWithMenuId() {
        // given
        Menu subMenu1_1 = Menu.builder()
                .name("1-1. 메뉴")
                .listOrder(1)
                .children(new ArrayList<>())
                .build();

        Menu mainMenu1 = Menu.builder()
                .name("1. 메뉴")
                .listOrder(1)
                .children(List.of(subMenu1_1))
                .build();

        menuRepository.save(mainMenu1);

        PostCreateServiceRequest request = PostCreateServiceRequest.builder()
                .title("글제목 입니다.")
                .content("글내용 입니다.")
                .menuId(Math.toIntExact(mainMenu1.getId()))
                .build();

        // when
        postService.createPost(request);

        // then
        List<Post> posts = postRepository.findAll();
        assertThat(posts).hasSize(1)
                .extracting("title", "content", "menu")
                .contains(
                        tuple("글제목 입니다.", "글내용 입니다.", mainMenu1)
                );
    }

    @DisplayName("존재하지 않는 munuId로 새로운 글을 등록하면 예외가 발생한다.")
    @Test
    void createPostWithNonExistingMenuId() {
        // given
        PostCreateServiceRequest request = PostCreateServiceRequest.builder()
                .title("글제목 입니다.")
                .content("글내용 입니다.")
                .menuId(1)
                .build();

        // when // then
        List<Post> posts = postRepository.findAll();
        assertThatThrownBy(() -> postService.createPost(request))
                .isInstanceOf(MenuNotFound.class)
                .hasMessage("존재하지 않는 메뉴입니다.");
    }

    @DisplayName("글 1개 조회한다.")
    @Test
    void getPost() {
        // given
        Post post = Post.builder()
                .title("글제목 입니다.")
                .content("글내용 입니다.")
                .build();
        postRepository.save(post);

        // when
        PostResponse response = postService.getPost(post.getId());

        // then
        assertThat(response)
                .extracting("title", "content")
                .contains("글제목 입니다.", "글내용 입니다.");
    }

    @DisplayName("존재하지 않는 글ID로 글을 조회하면 예외가 발생한다.")
    @Test
    void getPostByNonExistingId() {
        // given
        Post post = Post.builder()
                .title("글제목 입니다.")
                .content("글내용 입니다.")
                .build();
        postRepository.save(post);

        // when // then
        assertThatThrownBy(() -> postService.getPost(post.getId() + 1))
                .isInstanceOf(PostNotFound.class)
                .hasMessage("존재하지 않는 글입니다.");
    }

    @DisplayName("서브 메뉴에 속한 글들을 Id 내림차순으로 조회한다.")
    @Test
    void getSubMenuPostList() {
        // given
        Menu subMenu1_1 = Menu.builder()
                .name("1-1. 메뉴")
                .listOrder(1)
                .children(new ArrayList<>())
                .build();

        Menu mainMenu1 = Menu.builder()
                .name("1. 메뉴")
                .listOrder(1)
                .children(List.of(subMenu1_1))
                .build();

        Post post1 = Post.builder()
                .title("글제목1")
                .content("글내용1")
                .menu(subMenu1_1)
                .build();
        Post post2 = Post.builder()
                .title("글제목2")
                .content("글내용2")
                .menu(subMenu1_1)
                .build();
        Post post3 = Post.builder()
                .title("글제목3")
                .content("글내용3")
                .menu(subMenu1_1)
                .build();

        menuRepository.save(mainMenu1);
        postRepository.saveAll(List.of(post1, post2, post3));

        PostSearchServiceRequest request = PostSearchServiceRequest.builder()
                .page(1)
                .size(3)
                .menuId(Math.toIntExact(subMenu1_1.getId()))
                .build();

        // when
        List<PostResponse> postList = postService.getPostList(request);

        // then
        assertThat(postList).hasSize(3)
                .extracting("title", "content")
                .containsExactly(
                        tuple("글제목3", "글내용3"),
                        tuple("글제목2", "글내용2"),
                        tuple("글제목1", "글내용1")
                );
    }

    @DisplayName("메인 메뉴에 속한 글들을 Id 내림차순으로 조회한다.")
    @Test
    void getMAinMenuPostList() {
        // given
        Menu subMenu1_1 = Menu.builder()
                .name("1-1. 메뉴")
                .listOrder(1)
                .children(new ArrayList<>())
                .build();

        Menu subMenu1_2 = Menu.builder()
                .name("1-2. 메뉴")
                .listOrder(2)
                .children(new ArrayList<>())
                .build();

        Menu mainMenu1 = Menu.builder()
                .name("1. 메뉴")
                .listOrder(1)
                .children(List.of(subMenu1_1, subMenu1_2))
                .build();

        Post post1 = Post.builder()
                .title("글제목1")
                .content("글내용1")
                .menu(subMenu1_1)
                .build();
        Post post2 = Post.builder()
                .title("글제목2")
                .content("글내용2")
                .menu(subMenu1_1)
                .build();
        Post post3 = Post.builder()
                .title("글제목3")
                .content("글내용3")
                .menu(subMenu1_2)
                .build();

        menuRepository.save(mainMenu1);
        postRepository.saveAll(List.of(post1, post2, post3));

        PostSearchServiceRequest request = PostSearchServiceRequest.builder()
                .page(1)
                .size(3)
                .menuId(Math.toIntExact(mainMenu1.getId()))
                .build();

        // when
        List<PostResponse> postList = postService.getPostList(request);

        // then
        assertThat(postList).hasSize(3)
                .extracting("title", "content")
                .containsExactly(
                        tuple("글제목3", "글내용3"),
                        tuple("글제목2", "글내용2"),
                        tuple("글제목1", "글내용1")
                );
    }

    @DisplayName("글 제목을 수정한다.")
    @Test
    void editPostTitle() {
        // given
        Post post = Post.builder()
                .title("글제목")
                .content("글내용")
                .build();
        postRepository.save(post);

        em.flush(); // todo @Transactional 대신 tearDown() 쓸지 고민
        em.clear();

        PostEditServiceRequest request = PostEditServiceRequest.builder()
                .title("글제목 수정")
                .content("글내용")
                .build();

        // when
        postService.editPost(post.getId(), request);

        em.flush();
        em.clear();

        // then
        Post editedPost = postRepository.findById(post.getId())
                .orElseThrow(() -> new RuntimeException("글이 존재하지 않습니다. id = " + post.getId()));
        assertThat(editedPost.getTitle()).isEqualTo("글제목 수정");
        assertThat(editedPost.getContent()).isEqualTo("글내용");
    }

    @DisplayName("글 내용을 수정한다.")
    @Test
    void editPostContent() {
        // given
        Post post = Post.builder()
                .title("글제목")
                .content("글내용")
                .build();
        postRepository.save(post);

        em.flush(); // todo @Transactional 대신 tearDown() 쓸지 고민
        em.clear();

        PostEditServiceRequest request = PostEditServiceRequest.builder()
                .title("글제목")
                .content("글내용 수정")
                .build();

        // when
        postService.editPost(post.getId(), request);

        em.flush();
        em.clear();

        // then
        Post editedPost = postRepository.findById(post.getId())
                .orElseThrow(() -> new RuntimeException("글이 존재하지 않습니다. id = " + post.getId()));
        assertThat(editedPost.getTitle()).isEqualTo("글제목");
        assertThat(editedPost.getContent()).isEqualTo("글내용 수정");
    }

    @DisplayName("존재하지 않는 글ID의 글을 수정할 시 예외가 발생한다.")
    @Test
    void editPostByNonExistingId() {
        // given
        Post post = Post.builder()
                .title("글제목")
                .content("글내용")
                .build();
        postRepository.save(post);

        em.flush(); // todo @Transactional 대신 tearDown() 쓸지 고민
        em.clear();

        PostEditServiceRequest request = PostEditServiceRequest.builder()
                .title("글제목 수정")
                .content("글내용 수정")
                .build();

        // when // then
        assertThatThrownBy(() -> postService.editPost(post.getId() + 1, request))
                .isInstanceOf(PostNotFound.class)
                .hasMessage("존재하지 않는 글입니다.");

        em.flush();
        em.clear();

        Post editedPost = postRepository.findById(post.getId())
                .orElseThrow(() -> new RuntimeException("글이 존재하지 않습니다. id = " + post.getId()));
        assertThat(editedPost.getTitle()).isEqualTo("글제목");
        assertThat(editedPost.getContent()).isEqualTo("글내용");
    }

    @DisplayName("글을 삭제한다.")
    @Test
    void deletePost() {
        // given
        Post post = Post.builder()
                .title("글제목")
                .content("글내용")
                .build();
        postRepository.save(post);

        // when
        postService.deletePost(post.getId());

        // then
        assertThat(postRepository.count()).isEqualTo(0);
    }
}