package jimuanco.jimslog.api.controller.post;

import jakarta.validation.Valid;
import jimuanco.jimslog.api.DataResponse;
import jimuanco.jimslog.api.controller.post.request.PostCreateRequest;
import jimuanco.jimslog.api.controller.post.request.PostEditRequest;
import jimuanco.jimslog.api.service.post.PostService;
import jimuanco.jimslog.api.service.post.request.PostSearchServiceRequest;
import jimuanco.jimslog.api.service.post.response.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/posts")
    public void createPost(@Valid @RequestBody PostCreateRequest postCreateRequest) {
        postService.createPost(postCreateRequest.toServiceRequest());
    }

    @GetMapping("/posts/{postId}")
    public DataResponse<PostResponse> getPost(@PathVariable(name = "postId") Long postId) {
        return DataResponse.ok(postService.getPost(postId));
    }

    @GetMapping("/posts")
    public DataResponse<List<PostResponse>> getPostList( //todo 왜 ModelAttribute에 생성자 방식이 안먹히지?
            @RequestParam(name = "page", required = false, defaultValue = "1") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10") int size) {
        PostSearchServiceRequest serviceRequest = PostSearchServiceRequest.builder()
                .page(page)
                .size(size)
                .build();
        return DataResponse.ok(postService.getPostList(serviceRequest));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping("/posts/{postId}")
    public void editPost(@PathVariable(name = "postId") Long postId,
                         @Valid @RequestBody PostEditRequest postEditRequest) {
        postService.editPost(postId, postEditRequest.toServiceRequest());
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/posts/{postId}")
    public void deletePost(@PathVariable(name = "postId") Long postId) {
        postService.deletePost(postId);
    }
}
