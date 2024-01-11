package jimuanco.jimslog.docs.menu;

import jimuanco.jimslog.api.controller.menu.MenuController;
import jimuanco.jimslog.api.controller.menu.request.MenuRequest;
import jimuanco.jimslog.api.service.menu.MenuService;
import jimuanco.jimslog.docs.RestDocsSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MenuControllerDocsTest extends RestDocsSupport {

    private final MenuService menuService = mock(MenuService.class);

    @Override
    protected Object initController() {
        return new MenuController(menuService);
    }

    @DisplayName("새로운 메뉴를 생성하는 API")
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
                .andExpect(status().isOk())
                .andDo(document("menus-create",
                        preprocessRequest(prettyPrint()),
                        requestFields(
                                fieldWithPath("[]").type(JsonFieldType.ARRAY)
                                        .description("메인 메뉴들"),
                                fieldWithPath("[].id").type(JsonFieldType.NUMBER)
                                        .description("메인 메뉴 아이디").optional(),
                                fieldWithPath("[].name").type(JsonFieldType.STRING)
                                        .description("메인 메뉴 이름"),
                                fieldWithPath("[].listOrder").type(JsonFieldType.NUMBER)
                                        .description("메인 메뉴 순서"),
                                fieldWithPath("[].children[]").type(JsonFieldType.ARRAY)
                                        .description("서브 메뉴들"),
                                fieldWithPath("[].children[].id").type(JsonFieldType.NUMBER)
                                        .description("서브 메뉴 아이디").optional(),
                                fieldWithPath("[].children[].name").type(JsonFieldType.STRING)
                                        .description("서브 메뉴 이름"),
                                fieldWithPath("[].children[].listOrder").type(JsonFieldType.NUMBER)
                                        .description("서브 메뉴 순서"),
                                fieldWithPath("[].children[].children[]").type(JsonFieldType.ARRAY)
                                        .description("빈 리스트")
                        )
                ));
    }

//    @DisplayName("메뉴를 조회하는 API")
//    @Test
//    void getMenus() throws Exception {
//        // given
//        MenuResponse subMenu1_1 = MenuResponse.builder()
//                .id(2L)
//                .name("1-1. 메뉴")
//                .listOrder(1)
//                .children(new ArrayList<>())
//                .build();
//
//        MenuResponse subMenu1_2 = MenuResponse.builder()
//                .id(3L)
//                .name("1-2. 메뉴")
//                .listOrder(2)
//                .children(new ArrayList<>())
//                .build();
//
//        MenuResponse mainMenu1 = MenuResponse.builder()
//                .id(1L)
//                .name("1. 메뉴")
//                .listOrder(1)
//                .children(List.of(subMenu1_1, subMenu1_2))
//                .build();
//
//        MenuResponse subMenu2_1 = MenuResponse.builder()
//                .id(5L)
//                .name("2-1. 메뉴")
//                .listOrder(1)
//                .children(new ArrayList<>())
//                .build();
//
//        MenuResponse mainMenu2 = MenuResponse.builder()
//                .id(4L)
//                .name("2. 메뉴")
//                .listOrder(2)
//                .children(List.of(subMenu2_1))
//                .build();
//
//        MenuResponse mainMenu3 = MenuResponse.builder()
//                .id(6L)
//                .name("3. 메뉴")
//                .listOrder(3)
//                .children(new ArrayList<>())
//                .build();
//
//        given(menuService.getMenus())
//                .willReturn(List.of(mainMenu1, mainMenu2, mainMenu3));
//
//        // when // then
//        mockMvc.perform(get("/menus"))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andDo(document("menus-list",
//                        preprocessResponse(prettyPrint()),
//                        responseFields(
//                                fieldWithPath("data[]").type(JsonFieldType.ARRAY)
//                                        .description("메인 메뉴들"),
//                                fieldWithPath("data[].id").type(JsonFieldType.NUMBER)
//                                        .description("메인 메뉴 아이디"),
//                                fieldWithPath("data[].name").type(JsonFieldType.STRING)
//                                        .description("메인 메뉴 이름"),
//                                fieldWithPath("data[].listOrder").type(JsonFieldType.NUMBER)
//                                        .description("메인 메뉴 순서"),
//                                fieldWithPath("data[].postsCount").type(JsonFieldType.NUMBER)
//                                        .description("메인 메뉴 글 수"),
//                                fieldWithPath("data[].children[]").type(JsonFieldType.ARRAY)
//                                        .description("서브 메뉴들"),
//                                fieldWithPath("data[].children[].id").type(JsonFieldType.NUMBER)
//                                        .description("서브 메뉴 아이디"),
//                                fieldWithPath("data[].children[].name").type(JsonFieldType.STRING)
//                                        .description("서브 메뉴 이름"),
//                                fieldWithPath("data[].children[].listOrder").type(JsonFieldType.NUMBER)
//                                        .description("서브 메뉴 순서"),
//                                fieldWithPath("data[].children[].postsCount").type(JsonFieldType.NUMBER)
//                                        .description("서브 메뉴 글 수"),
//                                fieldWithPath("data[].children[].children[]").type(JsonFieldType.ARRAY)
//                                        .description("빈 리스트")
//                        )
//                ));
//    }
}
