package jimuanco.jimslog.api.service.menu.request;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class MenuServiceRequest {
    private Long id;
    private String name;
    private int listOrder;
    private List<MenuServiceRequest> children = new ArrayList<>();

    @Builder
    public MenuServiceRequest(Long id, String name, int listOrder, List<MenuServiceRequest> children) {
        this.id = id;
        this.name = name;
        this.listOrder = listOrder;
        this.children = children;
    }
}
