package jimuanco.jimslog.api.controller.post.request;

import jakarta.validation.constraints.NotBlank;
import jimuanco.jimslog.api.service.post.request.PostEditServiceRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostEditRequest {

    @NotBlank(message = "제목을 입력해주세요.")
    private String title;

    @NotBlank(message = "내용을 입력해주세요.")
    private String content;

    private int menuId;

    @Builder
    private PostEditRequest(String title, String content, int menuId) {
        this.title = title;
        this.content = content;
        this.menuId = menuId;
    }

    public PostEditServiceRequest toServiceRequest() {
        return PostEditServiceRequest.builder()
                .title(title)
                .content(content)
                .menuId(menuId)
                .build();
    }
}
