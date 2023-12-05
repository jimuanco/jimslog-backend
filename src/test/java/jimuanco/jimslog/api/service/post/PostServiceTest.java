package jimuanco.jimslog.api.service.post;

import jimuanco.jimslog.api.service.post.request.PostCreateServiceRequest;
import jimuanco.jimslog.api.service.post.response.PostResponse;
import jimuanco.jimslog.domain.post.Post;
import jimuanco.jimslog.domain.post.PostRepository;
import jimuanco.jimslog.exception.PostNotFound;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
class PostServiceTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @DisplayName("새로운 글을 등록한다.")
    @Test
    void createPost() {
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
                .extracting("title", "content")
                .contains(
                        tuple("글제목 입니다.", "글내용 입니다.")
                );
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

}