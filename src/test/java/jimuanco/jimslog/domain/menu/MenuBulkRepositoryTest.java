package jimuanco.jimslog.domain.menu;

import jakarta.annotation.PostConstruct;
import jimuanco.jimslog.api.controller.menu.MenuController;
import jimuanco.jimslog.api.controller.menu.request.MenuRequest;
import jimuanco.jimslog.config.SecurityConfig;
import jimuanco.jimslog.utils.JwtUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
@Transactional
@Import({SecurityConfig.class, JwtUtils.class})
@SpringBootTest
class MenuBulkRepositoryTest {

    @Autowired
    private MenuController menuController;

    @Autowired
    private MenuRepository menuRepository;

    @PostConstruct
    void setGlobalSecurityContext() {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    @WithMockUser(username = "jim@gmail.com", roles = {"ADMIN"})
    @DisplayName("메뉴 대량 생성 시간을 측정한다.")
    @Test
    void createBulkMenu() {
        // given
        List<MenuRequest> menuRequestList = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            MenuRequest menuRequest = MenuRequest.builder()
                    .name("test" + i)
                    .listOrder(i)
                    .children(new ArrayList<>())
                    .build();
            menuRequestList.add(menuRequest);
        }
        // when // then
        long startTime = System.currentTimeMillis();
        menuController.changeMenus(menuRequestList);
        long endTime = System.currentTimeMillis();
        System.out.println("---------------------------------");
        System.out.printf("수행시간: %d\n", endTime - startTime);
        System.out.println("---------------------------------");
    }

    @WithMockUser(username = "jim@gmail.com", roles = {"ADMIN"})
    @DisplayName("멀티 스레드 환경에서 메뉴 대량 생성시 동시성 문제가 발생하지 않는다.")
    @Test
    void createBulkMenuInMultiThread() throws InterruptedException {
        // given
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        CountDownLatch countDownLatch = new CountDownLatch(5);

        List<MenuRequest> menuRequestList = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            MenuRequest menuRequest = MenuRequest.builder()
                    .name("menu" + i)
                    .listOrder(i)
                    .children(new ArrayList<>())
                    .build();
            menuRequestList.add(menuRequest);
        }

        // when
        for (int i = 1; i <= 5; i++) {
            executorService.execute(() -> {
                menuController.changeMenus(menuRequestList);
                countDownLatch.countDown();
            });
        }

        countDownLatch.await();

        // then
        List<Menu> menus = menuRepository.findAll();

        assertThat(menus).hasSize(250);

        int num = 1;
        for (Menu menu : menus) {
            assertThat(menu.getName()).isEqualTo("menu" + num);
            assertThat(menu.getListOrder()).isEqualTo(num++);
            if (num % 50 == 1) {
                num = 1;
            }
        }
    }

    @WithMockUser(username = "jim@gmail.com", roles = {"ADMIN"})
    @DisplayName("멀티 스레드 환경에서 상위 메뉴와 하위메뉴 대량 생성시 동시성 문제가 발생하지 않는다.")
    @Test
    void createBulkMainMenuAndSubMenuInMultiThread() throws InterruptedException {
        // given
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        CountDownLatch countDownLatch = new CountDownLatch(5);

        List<MenuRequest> menuRequestList = new ArrayList<>();

        MenuRequest sub1 = MenuRequest.builder()
                .name("sub" + 1)
                .listOrder(1)
                .build();
        MenuRequest sub2 = MenuRequest.builder()
                .name("sub" + 2)
                .listOrder(2)
                .build();

        for (int i = 1; i <= 50; i++) {
            MenuRequest menuRequest = MenuRequest.builder()
                    .name("main" + i)
                    .listOrder(i)
                    .children(List.of(sub1, sub2))
                    .build();
            menuRequestList.add(menuRequest);
        }

        // when
        for (int i = 1; i <= 5; i++) {
            executorService.execute(() -> {
                menuController.changeMenus(menuRequestList);
                countDownLatch.countDown();
            });
        }

        countDownLatch.await();

        // then
        List<Menu> menus = menuRepository.findAllMenus();
        boolean allChildrenHaveSizeTwo = menus.stream()
                .allMatch(menu -> menu.getChildren().size() == 2);

        assertThat(allChildrenHaveSizeTwo).isTrue();
    }

    @WithMockUser(username = "jim@gmail.com", roles = {"ADMIN"})
    @DisplayName("메뉴 대량 수정 시간을 측정한다.")
    @Test
    void updateBulkMenu() {
        // given
        List<MenuRequest> menuRequestList = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            MenuRequest menuRequest = MenuRequest.builder()
                    .name("test" + i)
                    .listOrder(i)
                    .children(new ArrayList<>())
                    .build();
            menuRequestList.add(menuRequest);
        }
        menuController.changeMenus(menuRequestList);

        // when // then
        List<MenuRequest> updatedMenuRequestList = new ArrayList<>();
        for (MenuRequest menuRequest : menuRequestList) {
            MenuRequest updatedMenuRequest = MenuRequest.builder()
                    .id(menuRequest.getId())
                    .name("수정")
                    .listOrder(menuRequest.getListOrder())
                    .children(menuRequest.getChildren())
                    .build();
            updatedMenuRequestList.add(updatedMenuRequest);
        }

        long startTime = System.currentTimeMillis();
        menuController.changeMenus(updatedMenuRequestList);
        long endTime = System.currentTimeMillis();
        System.out.println("---------------------------------");
        System.out.printf("수행시간: %d\n", endTime - startTime);
        System.out.println("---------------------------------");
    }

}