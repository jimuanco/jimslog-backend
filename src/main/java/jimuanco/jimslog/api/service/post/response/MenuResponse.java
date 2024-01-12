package jimuanco.jimslog.api.service.post.response;

import jimuanco.jimslog.domain.menu.Menu;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class MenuResponse {
    private Long id;
    private String name;
    private int listOrder;
    private int postsCount;
    private List<MenuResponse> children;

    @Builder
    public MenuResponse(Long id, String name, int listOrder, int postsCount, List<MenuResponse> children) {
        this.id = id;
        this.name = name;
        this.listOrder = listOrder;
        this.postsCount = postsCount;
        this.children = children;
    }

    public MenuResponse(Menu menu) { // todo Post랑 엮어서 단위테스트 작성
        this.id = menu.getId();
        this.name = menu.getName();
        this.listOrder = menu.getListOrder();
        this.children = menu.getChildren().stream()
                .map(child -> {
                    int count = child.getPostList().size();
                    this.postsCount += count;

                    return MenuResponse.builder()
                            .id(child.getId())
                            .name(child.getName())
                            .listOrder(child.getListOrder())
                            .postsCount(count)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
