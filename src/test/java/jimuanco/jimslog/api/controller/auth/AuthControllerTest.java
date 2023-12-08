package jimuanco.jimslog.api.controller.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jimuanco.jimslog.api.controller.auth.request.SignupRequest;
import jimuanco.jimslog.api.service.auth.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @DisplayName("회원가입을 한다.")
    @Test
    void signup() throws Exception {
        // given
        SignupRequest request = SignupRequest.builder()
                .name("이름")
                .email("jim@gmail.com")
                .password("1234")
                .build();
        String json = objectMapper.writeValueAsString(request);

        // when // then
        mockMvc.perform(post("/auth/signup")
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @DisplayName("회원가입 할때 이름은 필수값이다.")
    @Test
    void signupWithoutName() throws Exception {
        // given
        SignupRequest request = SignupRequest.builder()
                .email("jim@gmail.com")
                .password("1234")
                .build();
        String json = objectMapper.writeValueAsString(request);

        // when // then
        mockMvc.perform(post("/auth/signup")
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                .andExpect(jsonPath("$.validation.name").value("이름을 입력해주세요."));
    }

    @DisplayName("회원가입 할때 이메일은 필수값이다.")
    @Test
    void signupWithoutEmail() throws Exception {
        // given
        SignupRequest request = SignupRequest.builder()
                .name("이름")
                .password("1234")
                .build();
        String json = objectMapper.writeValueAsString(request);

        // when // then
        mockMvc.perform(post("/auth/signup")
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                .andExpect(jsonPath("$.validation.email").value("이메일을 입력해주세요."));
    }

    @DisplayName("회원가입 할때 비밀번호는 필수값이다.")
    @Test
    void signupWithoutPassword() throws Exception {
        // given
        SignupRequest request = SignupRequest.builder()
                .name("이름")
                .email("jim@gmail.com")
                .build();
        String json = objectMapper.writeValueAsString(request);

        // when // then
        mockMvc.perform(post("/auth/signup")
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                .andExpect(jsonPath("$.validation.password").value("비밀번호를 입력해주세요."));
    }

}