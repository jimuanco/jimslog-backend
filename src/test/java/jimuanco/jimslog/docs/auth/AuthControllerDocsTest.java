package jimuanco.jimslog.docs.auth;

import jakarta.servlet.http.HttpServletResponse;
import jimuanco.jimslog.api.controller.auth.AuthController;
import jimuanco.jimslog.api.controller.auth.request.LoginRequest;
import jimuanco.jimslog.api.controller.auth.request.SignupRequest;
import jimuanco.jimslog.api.service.auth.AuthService;
import jimuanco.jimslog.api.service.auth.request.LoginServiceRequest;
import jimuanco.jimslog.api.service.auth.response.TokenResponse;
import jimuanco.jimslog.docs.RestDocsSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import org.springframework.restdocs.payload.JsonFieldType;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.responseCookies;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthControllerDocsTest extends RestDocsSupport {

    private final AuthService authService = mock(AuthService.class);

    @Override
    protected Object initController() {
        return new AuthController(authService);
    }

    @DisplayName("회원가입 API")
    @Test
    void signup() throws Exception {
        SignupRequest request = SignupRequest.builder()
                .name("이름")
                .email("jim@gmail.com")
                .password("1234")
                .build();
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/auth/signup")
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("auth-signup",
                        preprocessRequest(prettyPrint()),
                        requestFields(
                                fieldWithPath("name").type(JsonFieldType.STRING)
                                        .description("이름"),
                                fieldWithPath("email").type(JsonFieldType.STRING)
                                        .description("이메일"),
                                fieldWithPath("password").type(JsonFieldType.STRING)
                                        .description("비밀번호")
                        )
                ));
    }

    @DisplayName("로그인 API") //todo 리팩토링
    @Test
    void login() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("jim@gmail.com")
                .password("1234")
                .build();
        String json = objectMapper.writeValueAsString(request);

        String accessToken = "JWT accessToken";
        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken(accessToken)
                .build();

        String refreshToken = UUID.randomUUID().toString();
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .maxAge(7 * 24 * 60 * 60)
                .path("/")
                .secure(true)
                .sameSite("None")
                .httpOnly(true)
                .build();

        given(authService.login(any(LoginServiceRequest.class), any(HttpServletResponse.class)))
                .willReturn(tokenResponse);

        mockMvc.perform(post("/auth/login")
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andDo(print())
                .andDo(result -> {
                    HttpServletResponse response = result.getResponse();
                    response.setHeader("Set-Cookie", cookie.toString());
                })
                .andExpect(status().isOk())
                .andDo(document("auth-login",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("email").type(JsonFieldType.STRING)
                                        .description("이메일"),
                                fieldWithPath("password").type(JsonFieldType.STRING)
                                        .description("비밀번호")
                        ),
                        responseFields(
                                fieldWithPath("data.accessToken").type(JsonFieldType.STRING)
                                        .description("Access Token")
                        ),
                        responseCookies(cookieWithName("refreshToken").description("Refresh Token"))
                ));
    }
}
