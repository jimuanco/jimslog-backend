package jimuanco.jimslog.api.controller.post.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostSearchRequestTest {

    @DisplayName("글 여러개 조회시 쿼리 파라미터로 page를 요청하지 않으면 기본값으로 1이 들어간다.")
    @Test
    void setPage() {
        // given
        PostSearchRequest postSearchRequest = new PostSearchRequest();

        // when
        postSearchRequest.changeToDefaultPage();

        // then
        assertThat(postSearchRequest.getPage()).isEqualTo(1);
    }

    @DisplayName("글 여러개 조회시 쿼리 파라미터로 size를 요청하지 않으면 기본값으로 10이 들어간다.")
    @Test
    void setSize() {
        // given
        PostSearchRequest postSearchRequest = new PostSearchRequest();

        // when
        postSearchRequest.changeToDefaultSize();

        // then
        assertThat(postSearchRequest.getSize()).isEqualTo(10);
    }
}