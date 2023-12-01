package jimuanco.jimslog.api.controller.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import jimuanco.jimslog.api.controller.post.request.PostCreateRequest;
import jimuanco.jimslog.api.service.post.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PostController.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostService postService;

    @DisplayName("새로운 글을 등록한다.")
    @Test
    void createPost() throws Exception {
        // given
        PostCreateRequest request = PostCreateRequest.builder()
                .title("글제목 입니다.")
                .content("글내용 입니다.")
                .build();
        String json = objectMapper.writeValueAsString(request);

        // when // then
        mockMvc.perform(post("/posts/new")
                .content(json)
                .contentType(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }
    
    @DisplayName("새로운 글을 등록할 때 제목은 필수값이다.")
    @Test
    void createPostWithoutTitle() throws Exception {
        // given
        PostCreateRequest request = PostCreateRequest.builder()
                .content("글내용 입니다.")
                .build();
        String json = objectMapper.writeValueAsString(request);

        // when // then
        mockMvc.perform(post("/posts/new")
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                .andExpect(jsonPath("$.validation.title").value("제목을 입력해주세요."));
    }

    @DisplayName("새로운 글을 등록할 때 내용은 필수값이다.")
    @Test
    void createPostWithoutContent() throws Exception {
        // given
        PostCreateRequest request = PostCreateRequest.builder()
                .title("글제목 입니다.")
                .build();
        String json = objectMapper.writeValueAsString(request);

        // when // then
        mockMvc.perform(post("/posts/new")
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                .andExpect(jsonPath("$.validation.content").value("내용을 입력해주세요."));
    }
}