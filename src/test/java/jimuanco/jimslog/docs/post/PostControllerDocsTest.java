package jimuanco.jimslog.docs.post;

import jimuanco.jimslog.api.controller.post.PostController;
import jimuanco.jimslog.api.controller.post.request.PostCreateRequest;
import jimuanco.jimslog.api.controller.post.request.PostEditRequest;
import jimuanco.jimslog.api.service.post.PostService;
import jimuanco.jimslog.api.service.post.S3Uploader;
import jimuanco.jimslog.api.service.post.request.PostSearchServiceRequest;
import jimuanco.jimslog.api.service.post.response.PostResponse;
import jimuanco.jimslog.docs.RestDocsSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PostControllerDocsTest extends RestDocsSupport {

    private final PostService postService = mock(PostService.class);
    private final S3Uploader s3Uploader = mock(S3Uploader.class);

    @Override
    protected Object initController() {
        return new PostController(postService, s3Uploader);
    }

    @DisplayName("새로운 글을 등록하는 API")
    @Test
    void createPost() throws Exception {
        PostCreateRequest request = PostCreateRequest.builder()
                .title("글제목 입니다.")
                .content("글내용 입니다.")
                .menuId(1)
                .uploadImageUrls(List.of("URL1", "URL2"))
                .deleteImageUrls(List.of("URL3", "URL4"))
                .build();
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/posts")
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(document("post-create",
                        preprocessRequest(prettyPrint()),
                        requestFields(
                                fieldWithPath("title").type(JsonFieldType.STRING)
                                        .description("글 제목"),
                                fieldWithPath("content").type(JsonFieldType.STRING)
                                        .description("글 내용"),
                                fieldWithPath("menuId").type(JsonFieldType.NUMBER)
                                        .description("메뉴 ID"),
                                fieldWithPath("uploadImageUrls").type(JsonFieldType.ARRAY)
                                        .description("최종적으로 등록할 Image URL"),
                                fieldWithPath("deleteImageUrls").type(JsonFieldType.ARRAY)
                                        .description("최종적으로 등록하지 않을 Image URL")
                        )
                ));
    }

    @DisplayName("글에 이미지를 등록하는 API")
    @Test
    void uploadPostImage() throws Exception {
        // given
        MockMultipartFile image = new MockMultipartFile("postImage",
                "image.png",
                "image/png",
                "<<image.png>>".getBytes());

        given(s3Uploader.upload(any(MockMultipartFile.class), anyString()))
                .willReturn("S3 Image URL");

        // when // then
        mockMvc.perform(multipart("/posts/image")
                        .file(image)
                        .contentType(MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("post-image-upload",
                        preprocessRequest(prettyPrint()),
                        requestParts(
                                partWithName("postImage").description("이미지 파일")
                        ),
                        responseFields(
                                fieldWithPath("data").type(JsonFieldType.STRING)
                                        .description("S3 Image URL")
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
                        .createdDateTime(LocalDateTime.now())
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
                                        .description("글 내용"),
                                fieldWithPath("data.createdDateTime").type(JsonFieldType.ARRAY)
                                        .description("글 작성 시간")
                        )
                ));
    }

    @DisplayName("글을 여러개 조회하는 API")
    @Test
    void getPostList() throws Exception {
        // given
        int page = 1;
        int size = 2;
        int offset = 1;

        List<PostResponse> response = LongStream.range(offset, offset + size)
                .mapToObj(i -> PostResponse.builder()
                        .id(i)
                        .title("글제목 " + i)
                        .content("글내용 " + i)
                        .createdDateTime(LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());


        given(postService.getPostList(any(PostSearchServiceRequest.class)))
                .willReturn(response);

        // when // then
        mockMvc.perform(MockMvcRequestBuilders.get("/posts")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("posts-list",
                        preprocessResponse(prettyPrint()),
                        queryParameters(
                                parameterWithName("page")
                                        .description("페이지").optional()
                                        .attributes(key("default").value("1")),
                                parameterWithName("size")
                                        .description("사이즈").optional()
                                        .attributes(key("default").value("10")),
                                parameterWithName("menu")
                                        .description("메뉴 ID, 0(default)으로 조회시 모든 게시글 조회").optional()
                                        .attributes(key("default").value("0"))
                        ),
                        responseFields(
                                fieldWithPath("data[0].id").type(JsonFieldType.NUMBER)
                                        .description("글 ID"),
                                fieldWithPath("data[0].title").type(JsonFieldType.STRING)
                                        .description("글 제목"),
                                fieldWithPath("data[0].content").type(JsonFieldType.STRING)
                                        .description("글 내용"),
                                fieldWithPath("data[0].createdDateTime").type(JsonFieldType.ARRAY)
                                        .description("글 작성 시간")
                        )
                ));

    }

    @DisplayName("글을 수정하는 API")
    @Test
    void editPost() throws Exception {
        Long postId = 1L;
        PostEditRequest request = PostEditRequest.builder()
                .title("글제목을 수정했습니다.")
                .content("글내용을 수정했습니다.")
                .menuId(2)
                .uploadImageUrls(List.of("URL1", "URL2"))
                .deleteImageUrls(List.of("URL3", "URL4"))
                .build();
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(patch("/posts/{postId}", postId)
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(document("post-edit",
                        preprocessRequest(prettyPrint()),
                        pathParameters(parameterWithName("postId")
                                .description("글 ID")
                        ),
                        requestFields(
                                fieldWithPath("title").type(JsonFieldType.STRING)
                                        .description("글 제목"),
                                fieldWithPath("content").type(JsonFieldType.STRING)
                                        .description("글 내용"),
                                fieldWithPath("menuId").type(JsonFieldType.NUMBER)
                                        .description("수정할 메뉴 ID"),
                                fieldWithPath("uploadImageUrls").type(JsonFieldType.ARRAY)
                                        .description("최종적으로 등록할 Image URL"),
                                fieldWithPath("deleteImageUrls").type(JsonFieldType.ARRAY)
                                        .description("최종적으로 등록하지 않을 Image URL")
                        )
                ));
    }

    @DisplayName("글을 삭제하는 API")
    @Test
    void deletePost() throws Exception {
        Long postId = 1L;

        mockMvc.perform(delete("/posts/{postId}", postId))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("post-delete",
                        pathParameters(parameterWithName("postId")
                                .description("글 ID")
                        )
                ));
    }
}