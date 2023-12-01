package jimuanco.jimslog.api.service.post.request;

import lombok.Builder;

public class PostCreateServiceRequest {
    private String title;
    private String content;

    @Builder
    private PostCreateServiceRequest(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
