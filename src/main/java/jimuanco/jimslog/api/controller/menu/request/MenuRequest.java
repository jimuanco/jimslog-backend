package jimuanco.jimslog.api.controller.menu.request;

import jimuanco.jimslog.api.service.menu.request.MenuServiceRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class MenuRequest {
    private Long id;
    private String name;
    private int listOrder;
    private List<MenuRequest> children = new ArrayList<>();

    @Builder
    public MenuRequest(Long id, String name, int listOrder, List<MenuRequest> children) {
        this.id = id;
        this.name = name;
        this.listOrder = listOrder;
        this.children = children;
    }

    public MenuServiceRequest toServiceRequest() {
        return MenuServiceRequest.builder()
                .id(id)
                .name(name)
                .listOrder(listOrder)
                .children(children !=  null ?
                        children.stream().map(MenuRequest::toServiceRequest).collect(Collectors.toList())
                        : null)
                .build();
    }
}
