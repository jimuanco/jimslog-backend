package jimuanco.jimslog.api.controller.post.request;

import jakarta.validation.constraints.NotBlank;
import jimuanco.jimslog.api.service.post.request.PostCreateServiceRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostCreateRequest {

    @NotBlank(message = "제목을 입력해주세요.")
    private String title;

    @NotBlank(message = "내용을 입력해주세요.")
    private String content;

    @Builder
    public PostCreateRequest(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public PostCreateServiceRequest toServiceRequest() {
        return PostCreateServiceRequest.builder()
                .title(title)
                .content(content)
                .build();
    }
}
