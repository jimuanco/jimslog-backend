package jimuanco.jimslog.api.service.post;

import com.amazonaws.services.s3.AmazonS3;
import jimuanco.jimslog.api.service.post.request.PostCreateServiceRequest;
import jimuanco.jimslog.api.service.post.request.PostEditServiceRequest;
import jimuanco.jimslog.api.service.post.request.PostSearchServiceRequest;
import jimuanco.jimslog.api.service.post.response.PostResponse;
import jimuanco.jimslog.domain.menu.Menu;
import jimuanco.jimslog.domain.menu.MenuRepository;
import jimuanco.jimslog.domain.post.Post;
import jimuanco.jimslog.domain.post.PostImage;
import jimuanco.jimslog.domain.post.PostImageRepository;
import jimuanco.jimslog.domain.post.PostRepository;
import jimuanco.jimslog.exception.MenuNotFound;
import jimuanco.jimslog.exception.PostNotFound;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class PostService {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${jimslog.s3.url}")
    private String s3Url;

    private final PostRepository postRepository;
    private final MenuRepository menuRepository;
    private final PostImageRepository postImageRepository;
    private final AmazonS3 amazonS3;

    @Transactional
    public void createPost(PostCreateServiceRequest serviceRequest) {
        long menuId = serviceRequest.getMenuId();
        Menu menu = (menuId != 0) ? menuRepository.findById(menuId).orElseThrow(MenuNotFound::new) : null;

        Post post = serviceRequest.toEntity(menu);
        postRepository.save(post);

        processImageUploadsForPost(serviceRequest.getUploadImageUrls(), post);
        deleteS3Images(serviceRequest.getDeleteImageUrls());
        deleteDbImages(serviceRequest.getDeleteImageUrls());
    }

    public PostResponse getPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(PostNotFound::new);

        return PostResponse.of(post);
    }

    public List<PostResponse> getPostList(PostSearchServiceRequest serviceRequest) {
        List<Long> menuIdList = new ArrayList<>();

        if(serviceRequest.getMenuId() != 0) {
            Menu menu = menuRepository.findById((long) serviceRequest.getMenuId())
                    .orElseThrow(MenuNotFound::new);
            menuIdList.addAll(menu.getChildren().stream().map(Menu::getId).collect(Collectors.toList()));
        }

        menuIdList.add((long) serviceRequest.getMenuId());

        return postRepository.getPostList(serviceRequest, menuIdList).stream()
                .map(PostResponse::of)
                .collect(Collectors.toList());
    }

    @Transactional
    public void editPost(Long postId, PostEditServiceRequest serviceRequest) {
        Post post = postRepository.findById(postId)
                .orElseThrow(PostNotFound::new);

        Menu menu = menuRepository.findById((long) serviceRequest.getMenuId())
                .orElseThrow(MenuNotFound::new);

        post.edit(serviceRequest.getTitle(), serviceRequest.getContent(), menu); // todo editor class 만들지 고민

        processImageUploadsForPost(serviceRequest.getUploadImageUrls(), post);
        deleteS3Images(serviceRequest.getDeleteImageUrls());
        deleteDbImages(serviceRequest.getDeleteImageUrls());
    }

    @Transactional
    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(PostNotFound::new);

        postRepository.delete(post);
    }

    private void processImageUploadsForPost(List<String> uploadImageUrls, Post post) {
        uploadImageUrls.stream()
                .map(uploadImageUrl -> extractFileNameFromImageUrl(uploadImageUrl))
                .forEach(fileName -> updatePostIdForPostImage(fileName, post.getId()));
    }

    private String extractFileNameFromImageUrl(String imageUrl) {
        imageUrl.substring(s3Url.length() + 1);
        String encodigString = imageUrl.substring(s3Url.length() + 1);
        String decodingString;
        try {
            decodingString = URLDecoder.decode(encodigString, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Image URL Decoding 실패.");
        }
        return decodingString;
    }

    private void updatePostIdForPostImage(String fileName, Long postId) {
        PostImage postImage = postImageRepository.findByFileName(fileName)
                .orElseThrow(() -> new RuntimeException("이미지 파일이 존재하지 않습니다."));
        postImage.updatePostId(postId);
    }

    private void deleteDbImages(List<String> deleteDbImageUrls) {
        List<String> deleteFileNames = deleteDbImageUrls.stream()
                .map(deleteImageUrl -> extractFileNameFromImageUrl(deleteImageUrl))
                .collect(Collectors.toList());
        postImageRepository.deleteAllByFileNameInQuery(deleteFileNames);
    }

    private void deleteS3Images(List<String> deleteImageUrls) {
        deleteImageUrls
                .forEach(deleteImageUrl -> {
                    String FileName = extractFileNameFromImageUrl(deleteImageUrl);
                    amazonS3.deleteObject(bucket, FileName);
                });
    }
}
