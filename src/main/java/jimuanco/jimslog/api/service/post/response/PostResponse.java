package jimuanco.jimslog.api.service.post.response;

import jimuanco.jimslog.domain.post.Post;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostResponse {

    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdDateTime;

    @Builder
    private PostResponse(Long id, String title, String content, LocalDateTime createdDateTime) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdDateTime = createdDateTime;
    }

    public static PostResponse of(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .createdDateTime(post.getCreatedDateTime())
                .build();
    }
}
