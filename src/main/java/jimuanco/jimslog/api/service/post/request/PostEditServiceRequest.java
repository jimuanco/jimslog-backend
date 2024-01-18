package jimuanco.jimslog.api.service.post.request;

import lombok.Builder;
import lombok.Getter;

@Getter
public class PostEditServiceRequest {

    private String title;
    private String content;
    private int menuId;

    @Builder
    private PostEditServiceRequest(String title, String content, int menuId) {
        this.title = title;
        this.content = content;
        this.menuId = menuId;
    }
}
