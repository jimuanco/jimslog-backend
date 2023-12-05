package jimuanco.jimslog.api.service.post.request;

import jimuanco.jimslog.domain.post.Post;
import lombok.Builder;

public class PostCreateServiceRequest {
    private String title;
    private String content;

    @Builder
    private PostCreateServiceRequest(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public Post toEntity() {
        return Post.builder()
                .title(this.title)
                .content(this.content)
                .build();
    }
}
