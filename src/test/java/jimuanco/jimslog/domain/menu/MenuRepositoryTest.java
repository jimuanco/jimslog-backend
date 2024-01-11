package jimuanco.jimslog.domain.menu;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class MenuRepositoryTest {

    @Autowired
    private MenuRepository menuRepository;

    @DisplayName("메뉴들을 ID로 한번에 삭제한다.")
    @Test
    void deleteAllMenusByIdInQuery() {
        // given
        Menu subMenu1_1 = Menu.builder()
                .name("1-1. 메뉴")
                .listOrder(1)
                .children(new ArrayList<>())
                .build();

        Menu mainMenu1 = Menu.builder()
                .name("1. 메뉴")
                .listOrder(1)
                .children(List.of(subMenu1_1))
                .build();

        Menu subMenu2_1 = Menu.builder()
                .name("2-2. 메뉴")
                .listOrder(1)
                .children(new ArrayList<>())
                .build();

        Menu mainMenu2 = Menu.builder()
                .name("2. 메뉴")
                .listOrder(2)
                .children(List.of(subMenu2_1))
                .build();

        menuRepository.saveAll(List.of(mainMenu1, mainMenu2));

        // when
        // 테스트에선 왜 부모 먼저 삭제해도 동작하는지 모르겠음.
        menuRepository.deleteAllByIdInQuery(List.of(subMenu1_1.getId(), subMenu2_1.getId()));
        menuRepository.deleteAllByIdInQuery(List.of(mainMenu1.getId(), mainMenu2.getId()));

        // then
        List<Menu> menus = menuRepository.findAll();

        assertThat(menus).hasSize(0);
    }
}