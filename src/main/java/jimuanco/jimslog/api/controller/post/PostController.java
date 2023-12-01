package jimuanco.jimslog.api.controller.post;

import jakarta.validation.Valid;
import jimuanco.jimslog.api.DataResponse;
import jimuanco.jimslog.api.controller.post.request.PostCreateRequest;
import jimuanco.jimslog.api.service.post.PostService;
import jimuanco.jimslog.api.service.post.response.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping("/posts/new")
    public void createPost(@Valid @RequestBody PostCreateRequest request) {
        postService.createPost(request.toServiceRequest());
    }

    @GetMapping("/posts/{postId}")
    public DataResponse<PostResponse> getPost(@PathVariable(name = "postId") Long postId) {
        return DataResponse.ok(postService.getPost(postId));
    }
}
