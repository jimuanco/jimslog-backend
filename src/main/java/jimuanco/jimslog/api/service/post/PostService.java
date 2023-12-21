package jimuanco.jimslog.api.service.post;

import jimuanco.jimslog.api.service.post.request.PostCreateServiceRequest;
import jimuanco.jimslog.api.service.post.request.PostEditServiceRequest;
import jimuanco.jimslog.api.service.post.request.PostSearchServiceRequest;
import jimuanco.jimslog.api.service.post.response.PostResponse;
import jimuanco.jimslog.domain.post.Post;
import jimuanco.jimslog.domain.post.PostRepository;
import jimuanco.jimslog.exception.PostNotFound;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;

    public void createPost(PostCreateServiceRequest serviceRequest) {
        Post post = serviceRequest.toEntity();
        postRepository.save(post);
    }

    public PostResponse getPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(PostNotFound::new);

        return PostResponse.of(post);
    }

    public List<PostResponse> getPostList(PostSearchServiceRequest serviceRequest) {
        return postRepository.getPostList(serviceRequest).stream()
                .map(PostResponse::of)
                .collect(Collectors.toList());
    }

    @Transactional
    public void editPost(Long postId, PostEditServiceRequest serviceRequest) {
        Post post = postRepository.findById(postId)
                .orElseThrow(PostNotFound::new);

        post.edit(serviceRequest.getTitle(), serviceRequest.getContent()); // todo editor class 만들지 고민
    }

    @Transactional
    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(PostNotFound::new);

        postRepository.delete(post);
    }
}
