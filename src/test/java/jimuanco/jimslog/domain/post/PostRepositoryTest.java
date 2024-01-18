package jimuanco.jimslog.domain.post;

import jimuanco.jimslog.api.service.post.request.PostSearchServiceRequest;
import jimuanco.jimslog.domain.menu.Menu;
import jimuanco.jimslog.domain.menu.MenuRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@SpringBootTest
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MenuRepository menuRepository;

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
        List<Post> postList = postRepository.getPostList(request, List.of(subMenu1_1.getId()));

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
    void getMainMenuPostList() {
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
        List<Post> postList =
                postRepository.getPostList(request, List.of(mainMenu1.getId(), subMenu1_1.getId(), subMenu1_2.getId()));

        // then
        assertThat(postList).hasSize(3)
                .extracting("title", "content")
                .containsExactly(
                        tuple("글제목3", "글내용3"),
                        tuple("글제목2", "글내용2"),
                        tuple("글제목1", "글내용1")
                );
    }

}