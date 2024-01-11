package jimuanco.jimslog;

import com.fasterxml.jackson.databind.ObjectMapper;
import jimuanco.jimslog.api.controller.auth.AuthController;
import jimuanco.jimslog.api.controller.menu.MenuController;
import jimuanco.jimslog.api.controller.post.PostController;
import jimuanco.jimslog.api.service.auth.AuthService;
import jimuanco.jimslog.api.service.menu.MenuService;
import jimuanco.jimslog.api.service.post.PostService;
import jimuanco.jimslog.config.SecurityConfig;
import jimuanco.jimslog.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@Import({SecurityConfig.class, JwtUtils.class})
@WebMvcTest(controllers = {
        AuthController.class,
        PostController.class,
        MenuController.class
})
public abstract class ControllerTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    protected PostService postService;

    @MockBean
    protected AuthService authService;

    @MockBean
    protected MenuService menuService;
}
