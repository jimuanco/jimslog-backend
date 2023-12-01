package jimuanco.jimslog.docs.post;

import jimuanco.jimslog.api.controller.post.PostController;
import jimuanco.jimslog.api.controller.post.request.PostCreateRequest;
import jimuanco.jimslog.api.service.post.PostService;
import jimuanco.jimslog.api.service.post.response.PostResponse;
import jimuanco.jimslog.docs.RestDocsSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
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

    @DisplayName("글을 1개 조회하는 API")
    @Test
    void getPost() throws Exception {
        // given
        Long postId = 1L;

        given(postService.getPost(anyLong()))
                .willReturn(PostResponse.builder()
                        .id(postId)
                        .title("글제목 입니다.")
                        .content("글내용 입니다.")
                        .build()
                );
        // when // then
        mockMvc.perform(get("/posts/{postId}", postId))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("post-inquiry",
                        preprocessResponse(prettyPrint()),
                        pathParameters(parameterWithName("postId")
                                .description("글 ID")
                        ),
                        responseFields(
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER)
                                        .description("글 ID"),
                                fieldWithPath("data.title").type(JsonFieldType.STRING)
                                        .description("글 제목"),
                                fieldWithPath("data.content").type(JsonFieldType.STRING)
                                        .description("글 내용")
                        )
                ));
    }
}