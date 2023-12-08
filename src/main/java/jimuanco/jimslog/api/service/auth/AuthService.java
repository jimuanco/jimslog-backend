package jimuanco.jimslog.api.service.auth;

import jakarta.servlet.http.HttpServletResponse;
import jimuanco.jimslog.api.service.auth.request.LoginServiceRequest;
import jimuanco.jimslog.api.service.auth.request.SignupServiceRequest;
import jimuanco.jimslog.api.service.auth.response.TokenResponse;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    public void signup(SignupServiceRequest serviceRequest) {

    }

    public TokenResponse login(LoginServiceRequest serviceRequest, HttpServletResponse response) {
        addRefreshTokenInCookie(response);
        return null;
    }

    private void addRefreshTokenInCookie(HttpServletResponse response) {

    }
}
