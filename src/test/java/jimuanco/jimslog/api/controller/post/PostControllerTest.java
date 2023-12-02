package jimuanco.jimslog.api.controller.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import jimuanco.jimslog.api.controller.post.request.PostCreateRequest;
import jimuanco.jimslog.api.service.post.PostService;
import jimuanco.jimslog.api.service.post.request.PostSearchServiceRequest;
import jimuanco.jimslog.api.service.post.response.PostResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    
    @DisplayName("글을 1개 조회한다.")
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
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.title").value("글제목 입니다."))
                .andExpect(jsonPath("$.data.content").value("글내용 입니다."));
    }

    @DisplayName("글을 여러개 조회한다.")
    @Test
    void getPostList() throws Exception {
        // given
        int page = 2;
        int size = 20;
        int offset = 21;

        List<PostResponse> response = LongStream.range(offset, offset + size)
                .mapToObj(i -> PostResponse.builder()
                        .id(i)
                        .title("글제목 " + i)
                        .content("글내용 " + i)
                        .build())
                .collect(Collectors.toList());


        given(postService.getPostList(any(PostSearchServiceRequest.class)))
                .willReturn(response);

        // when // then
        mockMvc.perform(get("/posts")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(response.size())); //todo 리스트 검증
    }

    @DisplayName("글을 여러개 조회할때 쿼리 파라미터로 page넣지 않으면 기본값인 1로 조회된다.")
    @Test
    void getPostListWithoutPage() throws Exception {
        // given
        int size = 20;
        int offset = 1;

        List<PostResponse> response = LongStream.range(offset, offset + size)
                .mapToObj(i -> PostResponse.builder()
                        .id(i)
                        .title("글제목 " + i)
                        .content("글내용 " + i)
                        .build())
                .collect(Collectors.toList());


        given(postService.getPostList(any(PostSearchServiceRequest.class)))
                .willReturn(response);

        // when // then
        mockMvc.perform(get("/posts")
                        .param("size", String.valueOf(size)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(response.size()));
    }

    @DisplayName("글을 여러개 조회할때 쿼리 파라미터로 size넣지 않으면 기본값인 10으로 조회된다.")
    @Test
    void getPostListWithoutSize() throws Exception {
        // given
        int page = 2;
        int size = 10;
        int offset = 11;

        List<PostResponse> response = LongStream.range(offset, offset + size)
                .mapToObj(i -> PostResponse.builder()
                        .id(i)
                        .title("글제목 " + i)
                        .content("글내용 " + i)
                        .build())
                .collect(Collectors.toList());


        given(postService.getPostList(any(PostSearchServiceRequest.class)))
                .willReturn(response);

        // when // then
        mockMvc.perform(get("/posts")
                        .param("page", String.valueOf(page)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(response.size()));
    }
}