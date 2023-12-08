package jimuanco.jimslog.api.controller.auth;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jimuanco.jimslog.api.DataResponse;
import jimuanco.jimslog.api.controller.auth.request.LoginRequest;
import jimuanco.jimslog.api.controller.auth.request.SignupRequest;
import jimuanco.jimslog.api.service.auth.AuthService;
import jimuanco.jimslog.api.service.auth.response.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public void signup(@Valid @RequestBody SignupRequest signupRequest) {
        authService.signup(signupRequest.toServiceRequest());
    }

    @PostMapping("/login")
    public DataResponse<TokenResponse> login(@Valid @RequestBody LoginRequest loginRequest,
                                             HttpServletResponse response) {
        return DataResponse.ok(authService.login(loginRequest.toServiceRequest(), response));
    }
}
