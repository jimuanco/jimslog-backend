package jimuanco.jimslog.api.service.post.request;

import lombok.Builder;

public class PostEditServiceRequest {

    private String title;
    private String content;

    @Builder
    private PostEditServiceRequest(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
