package jimuanco.jimslog.api.service.menu;

import jakarta.persistence.EntityManager;
import jimuanco.jimslog.IntegrationTestSupport;
import jimuanco.jimslog.api.service.menu.request.MenuServiceRequest;
import jimuanco.jimslog.api.service.post.response.MenuResponse;
import jimuanco.jimslog.domain.menu.Menu;
import jimuanco.jimslog.domain.menu.MenuRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@Transactional
class MenuServiceTest extends IntegrationTestSupport {

    @Autowired
    private EntityManager em;

    @Autowired
    private MenuService menuService;

    @Autowired
    private MenuRepository menuRepository;

    @DisplayName("새로운 메뉴를 생성한다.")
    @Test
    void createMenus() {
        // given
        MenuServiceRequest subMenu1_1 = MenuServiceRequest.builder()
                .name("1-1. 메뉴")
                .listOrder(1)
                .children(new ArrayList<>())
                .build();

        MenuServiceRequest subMenu1_2 = MenuServiceRequest.builder()
                .name("1-2. 메뉴")
                .listOrder(2)
                .children(new ArrayList<>())
                .build();

        MenuServiceRequest mainMenu1 = MenuServiceRequest.builder()
                .name("1. 메뉴")
                .listOrder(1)
                .children(List.of(subMenu1_1, subMenu1_2))
                .build();

        MenuServiceRequest subMenu2_1 = MenuServiceRequest.builder()
                .name("2-1. 메뉴")
                .listOrder(1)
                .children(new ArrayList<>())
                .build();

        MenuServiceRequest mainMenu2 = MenuServiceRequest.builder()
                .name("2. 메뉴")
                .listOrder(2)
                .children(List.of(subMenu2_1))
                .build();

        MenuServiceRequest mainMenu3 = MenuServiceRequest.builder()
                .name("3. 메뉴")
                .listOrder(3)
                .children(new ArrayList<>())
                .build();

        // when
        menuService.changeMenus(List.of(mainMenu1, mainMenu2, mainMenu3));

        // then
        List<Menu> menus = menuRepository.findAll();
        Menu savedMainMenu1 = menus.stream()
                .filter(menu -> menu.getName().equals("1. 메뉴"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("해당 메뉴가 존재하지 않습니다."));
        Menu savedSubMenu1_1 = menus.stream()
                .filter(menu -> menu.getName().equals("1-1. 메뉴"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("해당 메뉴가 존재하지 않습니다."));
        Menu savedSubMenu1_2 = menus.stream()
                .filter(menu -> menu.getName().equals("1-2. 메뉴"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("해당 메뉴가 존재하지 않습니다."));
        Menu savedMainMenu2 = menus.stream()
                .filter(menu -> menu.getName().equals("2. 메뉴"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("해당 메뉴가 존재하지 않습니다."));
        Menu savedSubMenu2_1 = menus.stream()
                .filter(menu -> menu.getName().equals("2-1. 메뉴"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("해당 메뉴가 존재하지 않습니다."));
        Menu savedMainMenu3 = menus.stream()
                .filter(menu -> menu.getName().equals("3. 메뉴"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("해당 메뉴가 존재하지 않습니다."));

        assertThat(menus).hasSize(6)
                .extracting("name", "listOrder")
                .contains(
                        tuple("1. 메뉴", 1),
                        tuple("1-1. 메뉴", 1),
                        tuple("1-2. 메뉴", 2),
                        tuple("2. 메뉴", 2),
                        tuple("2-1. 메뉴", 1),
                        tuple("3. 메뉴", 3)
                );
        assertThat(savedMainMenu1.getChildren()).hasSize(2)
                .extracting("name", "listOrder", "children")
                .contains(
                        tuple("1-1. 메뉴", 1, savedSubMenu1_1.getChildren()),
                        tuple("1-2. 메뉴", 2, savedSubMenu1_2.getChildren())
                );
        assertThat(savedSubMenu1_1.getParent()).isEqualTo(savedMainMenu1);
        assertThat(savedSubMenu1_2.getParent()).isEqualTo(savedMainMenu1);

        assertThat(savedMainMenu2.getChildren()).hasSize(1)
                .extracting("name", "listOrder", "children")
                .contains(
                        tuple("2-1. 메뉴", 1, savedSubMenu2_1.getChildren())
                );
        assertThat(savedSubMenu2_1.getParent()).isEqualTo(savedMainMenu2);

        assertThat(savedMainMenu3.getChildren()).hasSize(0);

    }

    @DisplayName("기존 메뉴의 이름을 변경한다.")
    @Test
    void changeMenuNames() {
        // given
        MenuServiceRequest subMenu1_1 = MenuServiceRequest.builder()
                .name("1-1. 메뉴")
                .listOrder(1)
                .children(new ArrayList<>())
                .build();

        MenuServiceRequest mainMenu1 = MenuServiceRequest.builder()
                .name("1. 메뉴")
                .listOrder(1)
                .children(List.of(subMenu1_1))
                .build();

        menuService.changeMenus(List.of(mainMenu1));
        Menu savedMainMenu1 = menuRepository.findByName("1. 메뉴")
                .orElseThrow(() -> new RuntimeException("해당 메뉴가 존재하지 않습니다."));
        Menu savedSubMenu1_1 = menuRepository.findByName("1-1. 메뉴")
                .orElseThrow(() -> new RuntimeException("해당 메뉴가 존재하지 않습니다."));

        MenuServiceRequest changedSubMenu1_1 = MenuServiceRequest.builder()
                .id(savedSubMenu1_1.getId())
                .name("1-1. 메뉴 변경")
                .listOrder(1)
                .children(new ArrayList<>())
                .build();

        MenuServiceRequest changedMainMenu1 = MenuServiceRequest.builder()
                .id(savedMainMenu1.getId())
                .name("1. 메뉴 변경")
                .listOrder(1)
                .children(List.of(changedSubMenu1_1))
                .build();

        // when
        menuService.changeMenus(List.of(changedMainMenu1));
        em.flush();
        em.clear();

        // then
        System.out.println("================");
        List<Menu> menus = menuRepository.findAll();
        System.out.println("================");
        Menu newMainMenu1 = menus.stream()
                .filter(menu -> menu.getName().equals("1. 메뉴 변경"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("해당 메뉴가 존재하지 않습니다."));
        Menu newSubMenu1_1 = menus.stream()
                .filter(menu -> menu.getName().equals("1-1. 메뉴 변경"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("해당 메뉴가 존재하지 않습니다."));

        assertThat(menus).hasSize(2)
                .extracting("name", "listOrder")
                .contains(
                        tuple("1. 메뉴 변경", 1),
                        tuple("1-1. 메뉴 변경", 1)
                );
        assertThat(newMainMenu1.getChildren()).hasSize(1)
                .extracting("name", "listOrder", "children")
                .contains(
                        tuple("1-1. 메뉴 변경", 1, newSubMenu1_1.getChildren())
                );
        assertThat(newSubMenu1_1.getParent()).isEqualTo(newMainMenu1);
    }

    @DisplayName("기존 메인 메뉴의 순서를 변경한다.")
    @Test
    void changeMainMenuOrders() {
        // given
        MenuServiceRequest subMenu1_1 = MenuServiceRequest.builder()
                .name("1-1. 메뉴")
                .listOrder(1)
                .children(new ArrayList<>())
                .build();

        MenuServiceRequest mainMenu1 = MenuServiceRequest.builder()
                .name("1. 메뉴")
                .listOrder(1)
                .children(List.of(subMenu1_1))
                .build();

        MenuServiceRequest mainMenu2 = MenuServiceRequest.builder()
                .name("2. 메뉴")
                .listOrder(2)
                .children(new ArrayList<>())
                .build();

        menuService.changeMenus(List.of(mainMenu1, mainMenu2));
        Menu savedMainMenu1 = menuRepository.findByName("1. 메뉴")
                .orElseThrow(() -> new RuntimeException("해당 메뉴가 존재하지 않습니다."));
        Menu savedSubMenu1_1 = menuRepository.findByName("1-1. 메뉴")
                .orElseThrow(() -> new RuntimeException("해당 메뉴가 존재하지 않습니다."));
        Menu savedMainMenu2 = menuRepository.findByName("2. 메뉴")
                .orElseThrow(() -> new RuntimeException("해당 메뉴가 존재하지 않습니다."));

        MenuServiceRequest changedSubMenu1_1 = MenuServiceRequest.builder()
                .id(savedSubMenu1_1.getId())
                .name("1-1. 메뉴")
                .listOrder(1)
                .children(new ArrayList<>())
                .build();

        MenuServiceRequest changedMainMenu1 = MenuServiceRequest.builder()
                .id(savedMainMenu1.getId())
                .name("1. 메뉴")
                .listOrder(2)
                .children(List.of(changedSubMenu1_1))
                .build();

        MenuServiceRequest changedMainMenu2 = MenuServiceRequest.builder()
                .id(savedMainMenu2.getId())
                .name("2. 메뉴")
                .listOrder(1)
                .children(new ArrayList<>())
                .build();

        // when
        menuService.changeMenus(List.of(changedMainMenu1, changedMainMenu2));
        em.flush();
        em.clear();

        // then
        System.out.println("================");
        List<Menu> menus = menuRepository.findAll();
        System.out.println("================");
        Menu newMainMenu1 = menus.stream()
                .filter(menu -> menu.getName().equals("2. 메뉴"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("해당 메뉴가 존재하지 않습니다."));
        Menu newMainMenu2 = menus.stream()
                .filter(menu -> menu.getName().equals("1. 메뉴"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("해당 메뉴가 존재하지 않습니다."));
        Menu newSubMenu2_1 = menus.stream()
                .filter(menu -> menu.getName().equals("1-1. 메뉴"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("해당 메뉴가 존재하지 않습니다."));

        assertThat(menus).hasSize(3)
                .extracting("name", "listOrder")
                .contains(
                        tuple("2. 메뉴", 1),
                        tuple("1. 메뉴", 2),
                        tuple("1-1. 메뉴", 1)
                );
        assertThat(newMainMenu1.getChildren()).hasSize(0);

        assertThat(newMainMenu2.getChildren()).hasSize(1)
                .extracting("name", "listOrder", "children")
                .contains(
                        tuple("1-1. 메뉴", 1, newSubMenu2_1.getChildren())
                );
        assertThat(newSubMenu2_1.getParent()).isEqualTo(newMainMenu2);
    }

    @DisplayName("기존 서브 메뉴의 순서를 변경한다.")
    @Test
    void changeSubMenuOrders() {
        // given
        MenuServiceRequest subMenu1_1 = MenuServiceRequest.builder()
                .name("1-1. 메뉴")
                .listOrder(1)
                .children(new ArrayList<>())
                .build();

        MenuServiceRequest subMenu1_2 = MenuServiceRequest.builder()
                .name("1-2. 메뉴")
                .listOrder(2)
                .children(new ArrayList<>())
                .build();

        MenuServiceRequest mainMenu1 = MenuServiceRequest.builder()
                .name("1. 메뉴")
                .listOrder(1)
                .children(List.of(subMenu1_1, subMenu1_2))
                .build();

        menuService.changeMenus(List.of(mainMenu1));
        Menu savedMainMenu1 = menuRepository.findByName("1. 메뉴")
                .orElseThrow(() -> new RuntimeException("해당 메뉴가 존재하지 않습니다."));
        Menu savedSubMenu1_1 = menuRepository.findByName("1-1. 메뉴")
                .orElseThrow(() -> new RuntimeException("해당 메뉴가 존재하지 않습니다."));
        Menu savedSubMenu1_2 = menuRepository.findByName("1-2. 메뉴")
                .orElseThrow(() -> new RuntimeException("해당 메뉴가 존재하지 않습니다."));

        MenuServiceRequest changedSubMenu1_1 = MenuServiceRequest.builder()
                .id(savedSubMenu1_2.getId())
                .name("1-2. 메뉴")
                .listOrder(1)
                .children(new ArrayList<>())
                .build();

        MenuServiceRequest changedSubMenu1_2 = MenuServiceRequest.builder()
                .id(savedSubMenu1_1.getId())
                .name("1-1. 메뉴")
                .listOrder(2)
                .children(new ArrayList<>())
                .build();

        MenuServiceRequest changedMainMenu1 = MenuServiceRequest.builder()
                .id(savedMainMenu1.getId())
                .name("1. 메뉴")
                .listOrder(1)
                .children(List.of(changedSubMenu1_1, changedSubMenu1_2))
                .build();

        // when
        menuService.changeMenus(List.of(changedMainMenu1));
        em.flush();
        em.clear();

        // then
        System.out.println("================");
        List<Menu> menus = menuRepository.findAll();
        System.out.println("================");
        Menu newMainMenu1 = menus.stream()
                .filter(menu -> menu.getName().equals("1. 메뉴"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("해당 메뉴가 존재하지 않습니다."));
        Menu newSubMenu1_1 = menus.stream()
                .filter(menu -> menu.getName().equals("1-2. 메뉴"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("해당 메뉴가 존재하지 않습니다."));
        Menu newSubMenu1_2 = menus.stream()
                .filter(menu -> menu.getName().equals("1-1. 메뉴"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("해당 메뉴가 존재하지 않습니다."));

        assertThat(menus).hasSize(3)
                .extracting("name", "listOrder")
                .contains(
                        tuple("1. 메뉴", 1),
                        tuple("1-2. 메뉴", 1),
                        tuple("1-1. 메뉴", 2)
                );
        assertThat(newMainMenu1.getChildren()).hasSize(2)
                .extracting("name", "listOrder", "children")
                .contains(
                        tuple("1-2. 메뉴", 1, newSubMenu1_1.getChildren()),
                        tuple("1-1. 메뉴", 2, newSubMenu1_2.getChildren())
                );;

        assertThat(newSubMenu1_1.getChildren()).hasSize(0);
        assertThat(newSubMenu1_1.getParent()).isEqualTo(newMainMenu1);

        assertThat(newSubMenu1_2.getChildren()).hasSize(0);
        assertThat(newSubMenu1_2.getParent()).isEqualTo(newMainMenu1);
    }

    @DisplayName("기존 메뉴를 삭제한다.")
    @Test
    void deleteMenus() {
        // given
        MenuServiceRequest subMenu1_1 = MenuServiceRequest.builder()
                .name("1-1. 메뉴")
                .listOrder(1)
                .children(new ArrayList<>())
                .build();

        MenuServiceRequest mainMenu1 = MenuServiceRequest.builder()
                .name("1. 메뉴")
                .listOrder(1)
                .children(List.of(subMenu1_1))
                .build();

        MenuServiceRequest mainMenu2 = MenuServiceRequest.builder()
                .name("2. 메뉴")
                .listOrder(1)
                .children(new ArrayList<>())
                .build();

        menuService.changeMenus(List.of(mainMenu1, mainMenu2));

        // when
        menuService.changeMenus(List.of());
        em.flush();
        em.clear();

        // then
        System.out.println("================");
        List<Menu> menus = menuRepository.findAll();
        System.out.println("================");

        assertThat(menus).hasSize(0);
    }

    @DisplayName("메인 메뉴와 하위 메뉴를 listOrder 순으로 조회한다.")
    @Test
    void getMenusOrderedByListOrder() {
        // given
        Menu mainMenu1 = Menu.builder()
                .name("1. 메뉴")
                .listOrder(1)
                .children(new ArrayList<>())
                .build();

        Menu subMenu1_1 = Menu.builder()
                .name("1-1. 메뉴")
                .listOrder(1)
                .children(new ArrayList<>())
                .parent(mainMenu1)
                .build();

        Menu subMenu1_2 = Menu.builder()
                .name("1-2. 메뉴")
                .listOrder(2)
                .children(new ArrayList<>())
                .parent(mainMenu1)
                .build();

        mainMenu1.addChildren(subMenu1_1);
        mainMenu1.addChildren(subMenu1_2);

        Menu mainMenu2 = Menu.builder()
                .name("2. 메뉴")
                .listOrder(2)
                .children(new ArrayList<>())
                .build();

        Menu subMenu2_1 = Menu.builder()
                .name("2-1. 메뉴")
                .listOrder(1)
                .children(new ArrayList<>())
                .parent(mainMenu2)
                .build();

        mainMenu2.addChildren(subMenu2_1);

        Menu mainMenu3 = Menu.builder()
                .name("3. 메뉴")
                .listOrder(3)
                .children(new ArrayList<>())
                .build();

        menuRepository.saveAll(List.of(mainMenu2, mainMenu3, mainMenu1));
        em.clear();

        // when
        List<MenuResponse> menus = menuService.getMenus();
        MenuResponse savedMainMenu1 = menus.stream()
                .filter(menu -> menu.getName().equals("1. 메뉴"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("해당 메뉴가 존재하지 않습니다."));
        MenuResponse savedMainMenu2 = menus.stream()
                .filter(menu -> menu.getName().equals("2. 메뉴"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("해당 메뉴가 존재하지 않습니다."));
        MenuResponse savedMainMenu3 = menus.stream()
                .filter(menu -> menu.getName().equals("3. 메뉴"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("해당 메뉴가 존재하지 않습니다."));

        assertThat(menus).hasSize(3)
                .extracting("name", "listOrder")
                .containsExactly(
                        tuple("1. 메뉴", 1),
                        tuple("2. 메뉴", 2),
                        tuple("3. 메뉴", 3)
                );

        assertThat(savedMainMenu1.getChildren()).hasSize(2)
                .extracting("name", "listOrder")
                .containsExactly(
                        tuple("1-1. 메뉴", 1),
                        tuple("1-2. 메뉴", 2)
                );

        assertThat(savedMainMenu2.getChildren()).hasSize(1)
                .extracting("name", "listOrder")
                .contains(
                        tuple("2-1. 메뉴", 1)
                );

        assertThat(savedMainMenu3.getChildren()).hasSize(0);
    }

}