package jimuanco.jimslog.api.service.menu;

import jimuanco.jimslog.api.service.menu.request.MenuServiceRequest;
import jimuanco.jimslog.api.service.post.response.MenuResponse;
import jimuanco.jimslog.domain.menu.Menu;
import jimuanco.jimslog.domain.menu.MenuBulkRepository;
import jimuanco.jimslog.domain.menu.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class MenuService {

    private final MenuRepository menuRepository;
    private final MenuBulkRepository menuBulkRepository;

    @Value("${bulk}")
    private boolean bulk;

    public List<MenuResponse> getMenus() {
        List<Menu> allMenus = menuRepository.findAllMenus();
        return allMenus.stream().map(MenuResponse::new).collect(Collectors.toList());
    }

    @Transactional
    public void changeMenus(List<MenuServiceRequest> serviceRequests) {
        List<Menu> oldMenus = menuRepository.findAllMenus();

        List<Menu> menusToInsert = new ArrayList<>();
        List<MenuServiceRequest> menusToUpdate = new ArrayList<>();
        List<Long> mainMenusToDelete = new ArrayList<>();
        List<Long> subMenusToDelete = new ArrayList<>();

        processMenuChanges(serviceRequests, oldMenus, null, menusToInsert, menusToUpdate,
                mainMenusToDelete, subMenusToDelete);

        if (menusToInsert.size() != 0) {
            menuBulkRepository.createMenus(menusToInsert);
        }

        if (menusToUpdate.size() != 0) {
            menuBulkRepository.updateMenus(menusToUpdate);
        }

        // 자식 먼저 삭제 한다.
        if (subMenusToDelete.size() != 0) {
            menuRepository.deleteAllByIdInQuery(subMenusToDelete);
        }

        if (mainMenusToDelete.size() != 0) {
            menuRepository.deleteAllByIdInQuery(mainMenusToDelete);
        }
    }

    private void processMenuChanges(List<MenuServiceRequest> serviceRequests, List<Menu> oldMenus, Menu parent,
                                    List<Menu> menusToInsert, List<MenuServiceRequest> menusToUpdate,
                                    List<Long> mainMenusToDelete, List<Long> subMenusToDelete) {
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
                createNewMenu(parent, newMenu, menusToInsert, menusToUpdate, mainMenusToDelete, subMenusToDelete);
                newMenusQueue.poll();
                continue;
            }

            Menu oldMenu = oldMenusQueue.poll();

            if (oldMenu.getId().equals(newMenu.getId())) {
                if (!oldMenu.getName().equals(newMenu.getName())) {
                    if (bulk) {
                        addMenuToUpdateList(menusToUpdate, oldMenu, newMenu);
                    }
                    else {
                        oldMenu.changeName(newMenu.getName());
                    }
                }
                if (oldMenu.getListOrder() != newMenu.getListOrder()) {
                    if (bulk) {
                        addMenuToUpdateList(menusToUpdate, oldMenu, newMenu);
                    }
                    else {
                        oldMenu.changeListOrder(newMenu.getListOrder());
                    }
                }

                // parent != null 이면 자식 메뉴는 존재하지 않는다.
                if (parent == null &&
                        ((newMenu.getChildren() != null && newMenu.getChildren().size() != 0) ||
                        (oldMenu.getChildren() != null && oldMenu.getChildren().size() != 0))) {
                    processMenuChanges(newMenu.getChildren(), oldMenu.getChildren(), oldMenu,
                            menusToInsert, menusToUpdate, mainMenusToDelete, subMenusToDelete);
                }

                newMenusQueue.poll();
            } else if (oldMenu.getId() < newMenu.getId()) {
                deleteMenu(parent, oldMenu, mainMenusToDelete, subMenusToDelete);
            }
        }

        while (!oldMenusQueue.isEmpty()) {
            Menu oldMenu = oldMenusQueue.poll();

            // parent != null 이면 자식 메뉴는 존재하지 않는다.
            if (parent == null && oldMenu.getChildren() != null && oldMenu.getChildren().size() != 0) {
                processMenuChanges(null, oldMenu.getChildren(), oldMenu,
                        menusToInsert, menusToUpdate, mainMenusToDelete, subMenusToDelete);
            }
            deleteMenu(parent, oldMenu, mainMenusToDelete, subMenusToDelete);
        }

        while (!newMenusQueue.isEmpty()) {
            MenuServiceRequest newMenu = newMenusQueue.poll();
            createNewMenu(parent, newMenu, menusToInsert, menusToUpdate, mainMenusToDelete, subMenusToDelete);

        }
    }

    private void createNewMenu(Menu parent, MenuServiceRequest newMenu,
                               List<Menu> menusToInsert, List<MenuServiceRequest> menusToUpdate,
                               List<Long> mainMenusToDelete, List<Long> subMenusToDelete) {
        Menu menu = Menu.builder()
                .name(newMenu.getName())
                .listOrder(newMenu.getListOrder())
                .parent(parent)
                .children(newMenu.getChildren() != null ? new ArrayList<>() : null)
                .build();

        if (newMenu.getChildren() != null && newMenu.getChildren().size() != 0) {
            processMenuChanges(newMenu.getChildren(), null, menu,
                    menusToInsert, menusToUpdate, mainMenusToDelete, subMenusToDelete);
        }

        if (parent == null) {
            if (bulk) {
                menusToInsert.add(menu);
            }
            else {
                menuRepository.save(menu);
            }
        } else {
            parent.addChildren(menu);
        }
    }

    private void addMenuToUpdateList(List<MenuServiceRequest> menusToUpdate, Menu oldMenu, MenuServiceRequest newMenu) {
        MenuServiceRequest menuToUpdate = MenuServiceRequest.builder()
                .id(oldMenu.getId())
                .name(newMenu.getName())
                .listOrder(newMenu.getListOrder())
                .build();
        menusToUpdate.add(menuToUpdate);
    }

    private void deleteMenu(Menu parent, Menu oldMenu, List<Long> mainMenusToDelete, List<Long> subMenusToDelete) {
        if (parent != null) {
            // @Query 사용 -> 부모 엔티티에서 연관 관계를 먼저 끊어 주지 않아도 된다.
            subMenusToDelete.add(oldMenu.getId());
        } else {
            mainMenusToDelete.add(oldMenu.getId());
        }
    }
}
