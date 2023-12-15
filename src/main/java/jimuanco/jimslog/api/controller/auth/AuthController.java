package jimuanco.jimslog.api.controller.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jimuanco.jimslog.api.DataResponse;
import jimuanco.jimslog.api.controller.auth.request.LoginRequest;
import jimuanco.jimslog.api.controller.auth.request.SignupRequest;
import jimuanco.jimslog.api.service.auth.AuthService;
import jimuanco.jimslog.api.service.auth.response.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @ResponseStatus(CREATED)
    @PostMapping("/signup")
    public void signup(@Valid @RequestBody SignupRequest signupRequest) {
        authService.signup(signupRequest.toServiceRequest());
    }

    @PostMapping("/login")
    public DataResponse<TokenResponse> login(@Valid @RequestBody LoginRequest loginRequest,
                                             HttpServletResponse response) {
        return DataResponse.of(authService.login(loginRequest.toServiceRequest(), response));
    }

    @PostMapping("/refresh")
    public DataResponse<TokenResponse> refresh(@CookieValue(value = "refreshToken") Cookie cookie,
                                               HttpServletResponse response) {
        String refreshToken = cookie.getValue();
        return DataResponse.of(authService.refresh(refreshToken, response));
    }
}
