package jimuanco.jimslog.api.service.auth;

import jakarta.servlet.http.HttpServletResponse;
import jimuanco.jimslog.api.service.auth.request.LoginServiceRequest;
import jimuanco.jimslog.api.service.auth.request.SignupServiceRequest;
import jimuanco.jimslog.api.service.auth.response.TokenResponse;
import jimuanco.jimslog.domain.user.User;
import jimuanco.jimslog.domain.user.UserRepository;
import jimuanco.jimslog.exception.EmailAlreadyExists;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void signup(SignupServiceRequest serviceRequest) {
        checkDuplicateEmail(serviceRequest.getEmail());

        String encodedPassword = passwordEncoder.encode(serviceRequest.getPassword());

        User user = User.builder()
                .name(serviceRequest.getName())
                .email(serviceRequest.getEmail())
                .password(encodedPassword)
                .build();
        userRepository.save(user);
    }

    public TokenResponse login(LoginServiceRequest serviceRequest, HttpServletResponse response) {
        addRefreshTokenInCookie(response);
        return null;
    }

    private void checkDuplicateEmail(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExists();
        }
    }

    private void addRefreshTokenInCookie(HttpServletResponse response) {

    }
}
