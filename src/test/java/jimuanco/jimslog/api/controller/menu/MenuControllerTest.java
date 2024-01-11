package jimuanco.jimslog.api.controller.menu;

import jimuanco.jimslog.ControllerTestSupport;
import jimuanco.jimslog.api.controller.menu.request.MenuRequest;
import jimuanco.jimslog.api.service.post.response.MenuResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MenuControllerTest extends ControllerTestSupport {

    @DisplayName("새로운 메뉴를 생성한다.")
    @Test
    void createMenus() throws Exception {
        // given
        MenuRequest subMenu1_1 = MenuRequest.builder()
                .name("1-1. 메뉴")
                .listOrder(1)
                .children(new ArrayList<>())
                .build();

        MenuRequest subMenu1_2 = MenuRequest.builder()
                .name("1-2. 메뉴")
                .listOrder(2)
                .children(new ArrayList<>())
                .build();

        MenuRequest mainMenu1 = MenuRequest.builder()
                .name("1. 메뉴")
                .listOrder(1)
                .children(List.of(subMenu1_1, subMenu1_2))
                .build();

        MenuRequest subMenu2_1 = MenuRequest.builder()
                .name("2-1. 메뉴")
                .listOrder(1)
                .children(new ArrayList<>())
                .build();

        MenuRequest mainMenu2 = MenuRequest.builder()
                .name("2. 메뉴")
                .listOrder(2)
                .children(List.of(subMenu2_1))
                .build();

        MenuRequest mainMenu3 = MenuRequest.builder()
                .name("3. 메뉴")
                .listOrder(3)
                .children(new ArrayList<>())
                .build();

        String json = objectMapper.writeValueAsString(List.of(mainMenu1, mainMenu2, mainMenu3));

        // when // then
        mockMvc.perform(post("/menus")
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @DisplayName("전체 메뉴를 조회한다.")
    @Test
    void getMenus() throws Exception {
        // given
        MenuResponse subMenu1_1 = MenuResponse.builder()
                .id(2L)
                .name("1-1. 메뉴")
                .listOrder(1)
                .children(new ArrayList<>())
                .build();

        MenuResponse subMenu1_2 = MenuResponse.builder()
                .id(3L)
                .name("1-2. 메뉴")
                .listOrder(2)
                .children(new ArrayList<>())
                .build();

        MenuResponse mainMenu1 = MenuResponse.builder()
                .id(1L)
                .name("1. 메뉴")
                .listOrder(1)
                .children(List.of(subMenu1_1, subMenu1_2))
                .build();

        MenuResponse subMenu2_1 = MenuResponse.builder()
                .id(5L)
                .name("2-1. 메뉴")
                .listOrder(1)
                .children(new ArrayList<>())
                .build();

        MenuResponse mainMenu2 = MenuResponse.builder()
                .id(4L)
                .name("2. 메뉴")
                .listOrder(2)
                .children(List.of(subMenu2_1))
                .build();

        MenuResponse mainMenu3 = MenuResponse.builder()
                .id(6L)
                .name("3. 메뉴")
                .listOrder(3)
                .children(new ArrayList<>())
                .build();

        given(menuService.getMenus())
                .willReturn(List.of(mainMenu1, mainMenu2, mainMenu3));

        // when // then
        mockMvc.perform(get("/menus"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].name").value("1. 메뉴"))
                .andExpect(jsonPath("$.data[0].children.length()").value(2))
                .andExpect(jsonPath("$.data[0].children[0].name").value("1-1. 메뉴"))
                .andExpect(jsonPath("$.data[0].children[1].name").value("1-2. 메뉴"))
                .andExpect(jsonPath("$.data[1].name").value("2. 메뉴"))
                .andExpect(jsonPath("$.data[1].children.length()").value(1))
                .andExpect(jsonPath("$.data[1].children[0].name").value("2-1. 메뉴"))
                .andExpect(jsonPath("$.data[2].name").value("3. 메뉴"))
                .andExpect(jsonPath("$.data[2].children.length()").value(0));
    }

}