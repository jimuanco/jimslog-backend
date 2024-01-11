package jimuanco.jimslog.api.service.menu;

import jimuanco.jimslog.api.service.menu.request.MenuServiceRequest;
import jimuanco.jimslog.domain.menu.Menu;
import jimuanco.jimslog.domain.menu.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class MenuService {

    private final MenuRepository menuRepository;
    private List<Long> mainMenusToDelete;
    private List<Long> subMenusToDelete;

//    public List<MenuResponse> getMenus() {
//        List<Menu> allMenus = menuRepository.findAllMenus();
//        return allMenus.stream().map(MenuResponse::new).collect(Collectors.toList());
//    }

    @Transactional
    public void changeMenus(List<MenuServiceRequest> serviceRequests) {
        List<Menu> oldMenus = menuRepository.findAllMenus();
        processMenuChanges(serviceRequests, oldMenus, null);

        // 자식 먼저 삭제 한다.
        if (subMenusToDelete != null) {
            menuRepository.deleteAllByIdInQuery(subMenusToDelete);
        }

        if (mainMenusToDelete != null) {
            menuRepository.deleteAllByIdInQuery(mainMenusToDelete);
        }
    }

    private void processMenuChanges(List<MenuServiceRequest> serviceRequests, List<Menu> oldMenus, Menu parent) {
        Queue<Menu> oldMenusQueue = (oldMenus != null)
                ? new LinkedList<>(oldMenus.stream()
                    .sorted(Comparator.comparing(Menu::getId))
                    .collect(Collectors.toList()))
                : new LinkedList<>();

        Queue<MenuServiceRequest> newMenusQueue = (serviceRequests != null)
                ? new LinkedList<>(serviceRequests.stream()
                    .sorted(Comparator.comparing(MenuServiceRequest::getId,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.toList()))
                : new LinkedList<>();

        while (!oldMenusQueue.isEmpty() && !newMenusQueue.isEmpty()) {
            MenuServiceRequest newMenu = newMenusQueue.peek();
            if (newMenu.getId() == null) {
                createNewMenu(parent, newMenu);
                newMenusQueue.poll();
                continue;
            }

            Menu oldMenu = oldMenusQueue.poll();

            if (oldMenu.getId().equals(newMenu.getId())) {
                if (!oldMenu.getName().equals(newMenu.getName())) {
                    oldMenu.changeName(newMenu.getName());
                }
                if (oldMenu.getListOrder() != newMenu.getListOrder()) {
                    oldMenu.changeListOrder(newMenu.getListOrder());
                }

                // parent != null 이면 자식 메뉴는 존재하지 않는다.
                if (parent == null &&
                        ((newMenu.getChildren() != null && newMenu.getChildren().size() != 0) ||
                        (oldMenu.getChildren() != null && oldMenu.getChildren().size() != 0))) {
                    processMenuChanges(newMenu.getChildren(), oldMenu.getChildren(), oldMenu);
                }

                newMenusQueue.poll();
            } else if (oldMenu.getId() < newMenu.getId()) {
                deleteMenu(parent, oldMenu);
            }
        }

        while (!oldMenusQueue.isEmpty()) {
            Menu oldMenu = oldMenusQueue.poll();

            // parent != null 이면 자식 메뉴는 존재하지 않는다.
            if (parent == null && oldMenu.getChildren() != null && oldMenu.getChildren().size() != 0) {
                processMenuChanges(null, oldMenu.getChildren(), oldMenu);
            }
            deleteMenu(parent, oldMenu);
        }

        while (!newMenusQueue.isEmpty()) {
            MenuServiceRequest newMenu = newMenusQueue.poll();
            createNewMenu(parent, newMenu);

        }
    }

    private void createNewMenu(Menu parent, MenuServiceRequest newMenu) {
        Menu menu = Menu.builder()
                .name(newMenu.getName())
                .listOrder(newMenu.getListOrder())
                .parent(parent)
                .children(newMenu.getChildren() != null ? new ArrayList<>() : null)
                .build();

        if (newMenu.getChildren() != null && newMenu.getChildren().size() != 0) {
            processMenuChanges(newMenu.getChildren(), null, menu);
        }

        if (parent == null) {
            menuRepository.save(menu);
        } else {
            parent.addChildren(menu);
        }
    }

    private void deleteMenu(Menu parent, Menu oldMenu) {
        if (parent != null) {
            // @Query 사용 -> 부모 엔티티에서 연관 관계를 먼저 끊어 주지 않아도 된다.
            if (subMenusToDelete == null) {
                subMenusToDelete = new ArrayList<>();
            }
            subMenusToDelete.add(oldMenu.getId());
        } else {
            if (mainMenusToDelete == null) {
                mainMenusToDelete = new ArrayList<>();
            }
            mainMenusToDelete.add(oldMenu.getId());
        }
    }
}
