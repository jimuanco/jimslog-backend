package jimuanco.jimslog.api.service.post.request;

import jimuanco.jimslog.domain.menu.Menu;
import jimuanco.jimslog.domain.post.Post;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class PostCreateServiceRequest {
    private String title;
    private String content;
    private int menuId;
    private List<String> uploadImageUrls;
    private List<String> deleteImageUrls;

    @Builder
    private PostCreateServiceRequest(String title, String content, int menuId,
                                     List<String> uploadImageUrls, List<String> deleteImageUrls) {
        this.title = title;
        this.content = content;
        this.menuId = menuId;
        this.uploadImageUrls = uploadImageUrls;
        this.deleteImageUrls = deleteImageUrls;
    }

    public Post toEntity(Menu menu) {
        return Post.builder()
                .title(this.title)
                .content(this.content)
                .menu(menu)
                .build();
    }
}
