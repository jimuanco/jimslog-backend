package jimuanco.jimslog.api.service.post.request;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class PostEditServiceRequest {

    private String title;
    private String content;
    private int menuId;
    private List<String> uploadImageUrls;
    private List<String> deleteImageUrls;

    @Builder
    private PostEditServiceRequest(String title, String content, int menuId,
                                   List<String> uploadImageUrls,
                                   List<String> deleteImageUrls) {
        this.title = title;
        this.content = content;
        this.menuId = menuId;
        this.uploadImageUrls = uploadImageUrls;
        this.deleteImageUrls = deleteImageUrls;
    }
}
