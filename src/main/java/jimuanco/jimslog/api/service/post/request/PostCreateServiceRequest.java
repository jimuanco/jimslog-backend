package jimuanco.jimslog.api.service.post.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

public class PostCreateServiceRequest {
    private String title;
    private String content;

    @Builder
    public PostCreateServiceRequest(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
