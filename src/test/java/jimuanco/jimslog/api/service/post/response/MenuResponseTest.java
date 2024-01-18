package jimuanco.jimslog.api.service.post.response;

import jimuanco.jimslog.domain.menu.Menu;
import jimuanco.jimslog.domain.post.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MenuResponseTest {

    @DisplayName("Menu 엔티티로 MenuResponse 객체 생성시 각 메뉴별 게시글 갯수가 계산된다.")
    @Test
    void calculatePostCountInMenuResponse() {
        // given
        Post post1 = Post.builder()
                .title("글제목1")
                .content("글내용1")
                .build();
        Post post2 = Post.builder()
                .title("글제목2")
                .content("글내용2")
                .build();
        Post post3 = Post.builder()
                .title("글제목3")
                .content("글내용3")
                .build();
        Post post4 = Post.builder()
                .title("글제목4")
                .content("글내용4")
                .build();

        Menu subMenu1_1 = Menu.builder()
                .name("1-1. 메뉴")
                .listOrder(1)
                .children(new ArrayList<>())
                .postList(List.of(post1, post2))
                .build();

        Menu subMenu1_2 = Menu.builder()
                .name("1-2. 메뉴")
                .listOrder(2)
                .children(new ArrayList<>())
                .postList(List.of(post3))
                .build();

        Menu mainMenu1 = Menu.builder()
                .name("1. 메뉴")
                .listOrder(1)
                .children(List.of(subMenu1_1, subMenu1_2))
                .postList(List.of(post4))
                .build();
        // when
        MenuResponse menuResponse = new MenuResponse(mainMenu1);

        // then
        assertThat(menuResponse.getPostsCount()).isEqualTo(4);
        assertThat(menuResponse.getChildren().get(0).getPostsCount()).isEqualTo(2);
        assertThat(menuResponse.getChildren().get(1).getPostsCount()).isEqualTo(1);
    }
}