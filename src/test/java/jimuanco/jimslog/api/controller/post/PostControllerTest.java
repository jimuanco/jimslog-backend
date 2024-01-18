package jimuanco.jimslog.api.controller.post;

import jimuanco.jimslog.ControllerTestSupport;
import jimuanco.jimslog.api.controller.post.request.PostCreateRequest;
import jimuanco.jimslog.api.controller.post.request.PostEditRequest;
import jimuanco.jimslog.api.service.post.request.PostSearchServiceRequest;
import jimuanco.jimslog.api.service.post.response.PostResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PostControllerTest extends ControllerTestSupport {

    @WithMockUser(username = "jim@gmail.com", roles = {"ADMIN"})
    @DisplayName("새로운 글을 등록한다.")
    @Test
    void createPost() throws Exception {
        // given
        int menuId= 1;
        PostCreateRequest request = PostCreateRequest.builder()
                .title("글제목 입니다.")
                .content("글내용 입니다.")
                .menuId(menuId)
                .build();
        String json = objectMapper.writeValueAsString(request);

        // when // then
        mockMvc.perform(post("/posts")
                .content(json)
                .contentType(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @DisplayName("새로운 글을 등록할때 인증되지 않은 사용자는 등록할 수 없다.")
    @Test
    void createPostForUnauthenticatedUser() throws Exception {
        // given
        int menuId= 1;
        PostCreateRequest request = PostCreateRequest.builder()
                .title("글제목 입니다.")
                .content("글내용 입니다.")
                .menuId(menuId)
                .build();
        String json = objectMapper.writeValueAsString(request);

        // when // then
        mockMvc.perform(post("/posts")
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("401"))
                .andExpect(jsonPath("$.message").value("유효하지 않은 Access Token입니다."));
    }

    @WithMockUser(username = "jim@gmail.com", roles = {"USER"})
    @DisplayName("유저 권한만 가진 사용자는 글을 등록할 수 없다.")
    @Test
    void createPostForUserWithUserRole() throws Exception {
        // given
        int menuId= 1;
        PostCreateRequest request = PostCreateRequest.builder()
                .title("글제목 입니다.")
                .content("글내용 입니다.")
                .menuId(menuId)
                .build();
        String json = objectMapper.writeValueAsString(request);

        // when // then
        mockMvc.perform(post("/posts")
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("403"))
                .andExpect(jsonPath("$.message").value("접근할 수 없습니다."));
    }
    
    @DisplayName("새로운 글을 등록할 때 제목은 필수값이다.")
    @Test
    void createPostWithoutTitle() throws Exception {
        // given
        int menuId= 1;
        PostCreateRequest request = PostCreateRequest.builder()
                .content("글내용 입니다.")
                .menuId(menuId)
                .build();
        String json = objectMapper.writeValueAsString(request);

        // when // then
        mockMvc.perform(post("/posts")
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
        int menuId= 1;
        PostCreateRequest request = PostCreateRequest.builder()
                .title("글제목 입니다.")
                .menuId(menuId)
                .build();
        String json = objectMapper.writeValueAsString(request);

        // when // then
        mockMvc.perform(post("/posts")
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
        int menuId = 1;
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
                        .param("size", String.valueOf(size))
                        .param("menu", String.valueOf(menuId)))
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
        int menuId = 1;
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
                        .param("size", String.valueOf(size))
                        .param("menu", String.valueOf(menuId)))
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
        int menuId = 1;
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
                        .param("page", String.valueOf(page))
                        .param("menu", String.valueOf(menuId)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(response.size()));
    }

    @WithMockUser(username = "jim@gmail.com", roles = {"ADMIN"})
    @DisplayName("글을 수정한다.")
    @Test
    void editPost() throws Exception {
        // given
        Long postId = 1L;
        PostEditRequest request = PostEditRequest.builder()
                .title("글제목을 수정했습니다.")
                .content("글내용을 수정했습니다.")
                .menuId(1)
                .build();
        String json = objectMapper.writeValueAsString(request);

        // when // then
        mockMvc.perform(patch("/posts/{postId}", postId)
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @WithMockUser(username = "jim@gmail.com", roles = {"ADMIN"})
    @DisplayName("글의 메뉴를 수정한다.")
    @Test
    void editPostMenu() throws Exception {
        // given
        Long postId = 1L;
        PostEditRequest request = PostEditRequest.builder()
                .title("글제목을 수정했습니다.")
                .content("글내용을 수정했습니다.")
                .menuId(2)
                .build();
        String json = objectMapper.writeValueAsString(request);

        // when // then
        mockMvc.perform(patch("/posts/{postId}", postId)
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @DisplayName("글을 수정할때 인증되지 않은 사용자는 수정할 수 없다.")
    @Test
    void editPostForUnauthenticatedUser() throws Exception {
        // given
        Long postId = 1L;
        PostEditRequest request = PostEditRequest.builder()
                .title("글제목을 수정했습니다.")
                .content("글내용을 수정했습니다.")
                .menuId(1)
                .build();
        String json = objectMapper.writeValueAsString(request);

        // when // then
        mockMvc.perform(patch("/posts/{postId}", postId)
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("401"))
                .andExpect(jsonPath("$.message").value("유효하지 않은 Access Token입니다."));
    }

    @WithMockUser(username = "jim@gmail.com", roles = {"USER"})
    @DisplayName("글을 수정할때 유저 권한만 가진 사용자는 글을 수정할 수 없다.")
    @Test
    void editPostForUserWithUserRole() throws Exception {
        // given
        Long postId = 1L;
        PostEditRequest request = PostEditRequest.builder()
                .title("글제목을 수정했습니다.")
                .content("글내용을 수정했습니다.")
                .menuId(1)
                .build();
        String json = objectMapper.writeValueAsString(request);

        // when // then
        mockMvc.perform(patch("/posts/{postId}", postId)
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("403"))
                .andExpect(jsonPath("$.message").value("접근할 수 없습니다."));
    }

    @DisplayName("글을 수정할때 제목은 필수값이다.")
    @Test
    void editPostWithoutTitle() throws Exception {
        // given
        Long postId = 1L;
        PostCreateRequest request = PostCreateRequest.builder()
                .content("글내용 입니다.")
                .menuId(1)
                .build();
        String json = objectMapper.writeValueAsString(request);

        // when // then
        mockMvc.perform(patch("/posts/{postId}", postId)
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                .andExpect(jsonPath("$.validation.title").value("제목을 입력해주세요."));
    }

    @DisplayName("글을 수정할때 내용은 필수값이다.")
    @Test
    void editPostWithoutContent() throws Exception {
        // given
        Long postId = 1L;
        PostCreateRequest request = PostCreateRequest.builder()
                .title("글제목 입니다.")
                .menuId(1)
                .build();
        String json = objectMapper.writeValueAsString(request);

        // when // then
        mockMvc.perform(patch("/posts/{postId}", postId)
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                .andExpect(jsonPath("$.validation.content").value("내용을 입력해주세요."));
    }

    @WithMockUser(username = "jim@gmail.com", roles = {"ADMIN"})
    @DisplayName("글을 삭제한다.")
    @Test
    void deletePost() throws Exception {
        // given
        Long postId = 1L;

        // when // then
        mockMvc.perform(delete("/posts/{postId}", postId))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @DisplayName("글을 삭제할때 인증되지 않은 사용자는 삭제할 수 없다.")
    @Test
    void deletePostForUnauthenticatedUser() throws Exception {
        // given
        Long postId = 1L;

        // when // then
        mockMvc.perform(delete("/posts/{postId}", postId))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("401"))
                .andExpect(jsonPath("$.message").value("유효하지 않은 Access Token입니다."));;
    }

    @WithMockUser(username = "jim@gmail.com", roles = {"USER"})
    @DisplayName("글을 삭제할때 유저 권한만 가진 사용자는 글을 삭제할 수 없다.")
    @Test
    void deletePostForUserWithUserRole() throws Exception {
        // given
        Long postId = 1L;

        // when // then
        mockMvc.perform(delete("/posts/{postId}", postId))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("403"))
                .andExpect(jsonPath("$.message").value("접근할 수 없습니다."));
    }
}