package jimuanco.jimslog.api.service.post;

import jimuanco.jimslog.api.service.post.request.PostCreateServiceRequest;
import jimuanco.jimslog.api.service.post.request.PostSearchServiceRequest;
import jimuanco.jimslog.api.service.post.response.PostResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {
    public void createPost(PostCreateServiceRequest serviceRequest) {

    }

    public PostResponse getPost(Long postId) {
        return null;
    }

    public List<PostResponse> getPostList(PostSearchServiceRequest serviceRequest) {
        return null;
    }
}
