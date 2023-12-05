package jimuanco.jimslog.api.service.post;

import jimuanco.jimslog.api.service.post.request.PostCreateServiceRequest;
import jimuanco.jimslog.api.service.post.request.PostEditServiceRequest;
import jimuanco.jimslog.api.service.post.request.PostSearchServiceRequest;
import jimuanco.jimslog.api.service.post.response.PostResponse;
import jimuanco.jimslog.domain.post.Post;
import jimuanco.jimslog.domain.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;

    public void createPost(PostCreateServiceRequest serviceRequest) {
        Post post = serviceRequest.toEntity();
        postRepository.save(post);
    }

    public PostResponse getPost(Long postId) {
        return null;
    }

    public List<PostResponse> getPostList(PostSearchServiceRequest serviceRequest) {
        return null;
    }

    public void editPost(Long postId, PostEditServiceRequest serviceRequest) {

    }

    public void deletePost(Long postId) {

    }
}
