package jimuanco.jimslog.domain.menu;

import jakarta.persistence.*;
import jimuanco.jimslog.domain.post.Post;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
@Entity
public class Menu {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "parent_id")
    private Menu parent;

    private String name;

    private int listOrder;

    @OneToMany(mappedBy = "parent", cascade = ALL)
    private List<Menu> children = new ArrayList<>();

    @OneToMany(mappedBy = "menu", cascade = ALL)
    private List<Post> postList = new ArrayList<>();

    @Builder
    public Menu(Menu parent, String name, int listOrder, List<Menu> children, List<Post> postList) {
        this.parent = parent;
        this.name = name;
        this.listOrder = listOrder;
        this.children = children;
        this.postList = postList;
    }

    public void addChildren(Menu menu) {
        children.add(menu);
    }

    public void changeListOrder(int listOrder) {
        this.listOrder = listOrder;
    }

    public void changeName(String name) {
        this.name = name;
    }
}
