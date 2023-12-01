package jimuanco.jimslog.docs.post;

import jimuanco.jimslog.api.controller.post.PostController;
import jimuanco.jimslog.api.controller.post.request.PostCreateRequest;
import jimuanco.jimslog.api.service.post.PostService;
import jimuanco.jimslog.docs.RestDocsSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

import static org.mockito.Mockito.mock;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PostControllerDocsTest extends RestDocsSupport {

    private final PostService postService = mock(PostService.class);

    @Override
    protected Object initController() {
        return new PostController(postService);
    }

    @DisplayName("새로운 글을 등록하는 API")
    @Test
    void createPost() throws Exception {
        PostCreateRequest request = PostCreateRequest.builder()
                .title("글제목 입니다.")
                .content("글내용 입니다.")
                .build();
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/posts/new")
                .content(json)
                .contentType(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("post-create",
                        preprocessRequest(prettyPrint()),
                        requestFields(
                                fieldWithPath("title").type(JsonFieldType.STRING)
                                        .description("글 제목"),
                                fieldWithPath("content").type(JsonFieldType.STRING)
                                        .description("글 내용")
                        )
                ));
    }
}