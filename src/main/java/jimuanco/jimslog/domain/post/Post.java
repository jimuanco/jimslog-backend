package jimuanco.jimslog.domain.post;

import jakarta.persistence.*;
import jimuanco.jimslog.domain.BaseEntity;
import jimuanco.jimslog.domain.menu.Menu;
import jimuanco.jimslog.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
@Entity
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @ManyToOne(fetch = LAZY)
    private User user;

    @ManyToOne(fetch = LAZY)
    private Menu menu;

    @Builder
    public Post(String title, String content, User user, Menu menu) {
        this.title = title;
        this.content = content;
        this.user = user;
        this.menu = menu;
    }

    public void edit(String title, String content, Menu menu) {
        this.title = title;
        this.content = content;
        this.menu = menu;
    }
}
