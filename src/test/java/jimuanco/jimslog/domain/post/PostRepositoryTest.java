package jimuanco.jimslog.domain.post;

import jimuanco.jimslog.api.service.post.request.PostSearchServiceRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@SpringBootTest
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @DisplayName("글 여러개를 조회할때 최신순으로 조회한다.")
    @Test
    void getPostList() {
        // given
        Post post1 = Post.builder()
                .title("글제목1")
                .content("글내용1")
                .build();
        Post post2 = Post.builder()
                .title("글제목2")
                .content("글내용2")
                .build();
        Post post3 = Post.builder()
                .title("글제목3")
                .content("글내용3")
                .build();

        postRepository.saveAll(List.of(post1, post2, post3));

        PostSearchServiceRequest request = PostSearchServiceRequest.builder()
                .page(1)
                .size(3)
                .build();

        // when
        List<Post> postList = postRepository.getPostList(request);

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