package jimuanco.jimslog.api.service.post.request;

import lombok.Builder;
import lombok.Getter;

@Getter
public class PostEditServiceRequest {

    private String title;
    private String content;

    @Builder
    private PostEditServiceRequest(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
