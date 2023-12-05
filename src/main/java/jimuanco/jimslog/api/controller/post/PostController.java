package jimuanco.jimslog.api.controller.post;

import jakarta.validation.Valid;
import jimuanco.jimslog.api.DataResponse;
import jimuanco.jimslog.api.controller.post.request.PostCreateRequest;
import jimuanco.jimslog.api.controller.post.request.PostEditRequest;
import jimuanco.jimslog.api.controller.post.request.PostSearchRequest;
import jimuanco.jimslog.api.service.post.PostService;
import jimuanco.jimslog.api.service.post.response.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping("/posts/new")
    public void createPost(@Valid @RequestBody PostCreateRequest postCreateRequest) {
        postService.createPost(postCreateRequest.toServiceRequest());
    }

    @GetMapping("/posts/{postId}")
    public DataResponse<PostResponse> getPost(@PathVariable(name = "postId") Long postId) {
        return DataResponse.ok(postService.getPost(postId));
    }

    @GetMapping("/posts")
    public DataResponse<List<PostResponse>> getPostList(@ModelAttribute PostSearchRequest postSearchRequest) {
        return DataResponse.ok(postService.getPostList(postSearchRequest.toServiceRequest()));
    }

    @PatchMapping("/posts/{postId}")
    public void editPost(@PathVariable Long postId, @RequestBody @Valid PostEditRequest postEditRequest) {
        postService.editPost(postId, postEditRequest.toServiceRequest());
    }
}
