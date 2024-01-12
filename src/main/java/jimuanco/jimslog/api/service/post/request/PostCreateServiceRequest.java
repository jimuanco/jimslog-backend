package jimuanco.jimslog.api.service.post.request;

import jimuanco.jimslog.domain.menu.Menu;
import jimuanco.jimslog.domain.post.Post;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PostCreateServiceRequest {
    private String title;
    private String content;
    private int menuId;

    @Builder
    private PostCreateServiceRequest(String title, String content, int menuId) {
        this.title = title;
        this.content = content;
        this.menuId = menuId;
    }

    public Post toEntity(Menu menu) {
        return Post.builder()
                .title(this.title)
                .content(this.content)
                .menu(menu)
                .build();
    }
}
