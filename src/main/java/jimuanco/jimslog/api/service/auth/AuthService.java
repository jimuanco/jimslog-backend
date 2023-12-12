package jimuanco.jimslog.api.service.auth;

import jakarta.servlet.http.HttpServletResponse;
import jimuanco.jimslog.api.service.auth.request.LoginServiceRequest;
import jimuanco.jimslog.api.service.auth.request.SignupServiceRequest;
import jimuanco.jimslog.api.service.auth.response.TokenResponse;
import jimuanco.jimslog.domain.user.RefreshToken;
import jimuanco.jimslog.domain.user.RefreshTokenRepository;
import jimuanco.jimslog.domain.user.User;
import jimuanco.jimslog.domain.user.UserRepository;
import jimuanco.jimslog.exception.EmailAlreadyExists;
import jimuanco.jimslog.exception.InvalidLoginInformation;
import jimuanco.jimslog.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RefreshTokenRepository refreshTokenRepository;

    private static final int REFRESH_DAYS = 30;

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

    @Transactional
    public TokenResponse login(LoginServiceRequest serviceRequest, HttpServletResponse response) {
        authenticateEmailAndPassword(serviceRequest);

        String accessToken = jwtUtils.generateAccessToken(serviceRequest.getEmail());
        Optional<RefreshToken> foundToken = refreshTokenRepository.findByUserEmail(serviceRequest.getEmail());

        String newRefreshToken = UUID.randomUUID().toString();

        if (foundToken.isPresent()) {
            foundToken.get().updateToken(newRefreshToken);
        } else {
            RefreshToken refreshToken = RefreshToken.builder()
                    .userEmail(serviceRequest.getEmail())
                    .refreshToken(newRefreshToken)
                    .expiryDate(LocalDateTime.now().plusDays(REFRESH_DAYS))
                    .build();
            refreshTokenRepository.save(refreshToken);
        }

        addRefreshTokenInCookie(response, newRefreshToken);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .build();
    }

    private void checkDuplicateEmail(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExists();
        }
    }

    private void authenticateEmailAndPassword(LoginServiceRequest serviceRequest) {
        User user = userRepository.findByEmail(serviceRequest.getEmail())
                .orElseThrow(InvalidLoginInformation::new);

        boolean matches = passwordEncoder.matches(serviceRequest.getPassword(), user.getPassword());
        if (!matches) {
            throw new InvalidLoginInformation();
        }
    }

    private void addRefreshTokenInCookie(HttpServletResponse response, String newToken) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", newToken)
                .maxAge(REFRESH_DAYS * 24 * 60 * 60)
                .path("/")
                .secure(true)
                .sameSite("None")
                .httpOnly(true)
                .build();

        response.setHeader("Set-Cookie", cookie.toString());
    }
}
