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

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;
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
        LocalDateTime expiryDate = now().plusDays(30);
        return DataResponse.of(authService.login(loginRequest.toServiceRequest(), response, expiryDate));
    }

    @PostMapping("/refresh")
    public DataResponse<TokenResponse> refresh(@CookieValue(value = "refreshToken", required = false) Cookie cookie,
                                               HttpServletResponse response) {
        if (cookie == null) {
            return handleNoCookieCase(response);
        }

        String refreshToken = cookie.getValue();

        LocalDateTime now = now();
        Long minimumExpiration = 60 * 60 * 24 * 7L;
        LocalDateTime expiryDate = now.plusDays(30);

        return DataResponse.of(authService.refresh(refreshToken, response, minimumExpiration, expiryDate));
    }

    @PostMapping("/logout")
    public void logout(@CookieValue(value = "refreshToken") Cookie cookie, HttpServletResponse response) {
        String refreshToken = cookie.getValue();
        authService.logout(refreshToken, response);
    }

    private DataResponse<TokenResponse> handleNoCookieCase(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_OK);
        return null;
    }
}
