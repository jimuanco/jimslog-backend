package jimuanco.jimslog.api.controller.menu;

import jimuanco.jimslog.api.DataResponse;
import jimuanco.jimslog.api.controller.menu.request.MenuRequest;
import jimuanco.jimslog.api.service.menu.MenuService;
import jimuanco.jimslog.api.service.post.response.MenuResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/menus")
    public DataResponse<List<MenuResponse>> getMenus() {
        List<MenuResponse> menus = menuService.getMenus();
        int count = menus.stream().mapToInt(MenuResponse::getPostsCount).sum();
        return DataResponse.of(menus, count);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/menus")
    public void changeMenus(@RequestBody List<MenuRequest> menuRequests) {
        menuService.changeMenus(menuRequests.stream()
                        .map(MenuRequest::toServiceRequest)
                        .collect(Collectors.toList()));
    }
}
