package jimuanco.jimslog.api.service.auth;

import jakarta.servlet.http.HttpServletResponse;
import jimuanco.jimslog.api.service.auth.request.LoginServiceRequest;
import jimuanco.jimslog.api.service.auth.request.SignupServiceRequest;
import jimuanco.jimslog.api.service.auth.response.TokenResponse;
import jimuanco.jimslog.domain.user.*;
import jimuanco.jimslog.exception.EmailAlreadyExists;
import jimuanco.jimslog.exception.InvalidLoginInformation;
import jimuanco.jimslog.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Transactional(readOnly = true)
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RefreshTokenRepository refreshTokenRepository;

    private final String ADMIN_ID;
    private static final int REFRESH_DAYS = 30;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtils jwtUtils,
                       RefreshTokenRepository refreshTokenRepository,
                       @Value("${jimslog.admin}") String adminId) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.refreshTokenRepository = refreshTokenRepository;
        this.ADMIN_ID = adminId;
    }

    public void signup(SignupServiceRequest serviceRequest) {
        checkDuplicateEmail(serviceRequest.getEmail());

        String encodedPassword = passwordEncoder.encode(serviceRequest.getPassword());
        Role role = serviceRequest.getEmail().equals(ADMIN_ID) ? Role.ADMIN : Role.USER;

        User user = User.builder()
                .name(serviceRequest.getName())
                .email(serviceRequest.getEmail())
                .password(encodedPassword)
                .role(role)
                .build();
        userRepository.save(user);
    }

    @Transactional
    public TokenResponse login(LoginServiceRequest serviceRequest, HttpServletResponse response) {
        User user = authenticateEmailAndPassword(serviceRequest);

        String accessToken = jwtUtils.generateAccessToken(user);
        Optional<RefreshToken> foundToken = refreshTokenRepository.findByUserEmail(user.getEmail());

        String newRefreshToken = UUID.randomUUID().toString();

        if (foundToken.isPresent()) {
            foundToken.get().updateToken(newRefreshToken);
        } else {
            RefreshToken refreshToken = RefreshToken.builder()
                    .userEmail(user.getEmail())
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

    public TokenResponse refresh(String refreshToken, HttpServletResponse response) {
        return null;
    }

    private void checkDuplicateEmail(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExists();
        }
    }

    private User authenticateEmailAndPassword(LoginServiceRequest serviceRequest) {
        User user = userRepository.findByEmail(serviceRequest.getEmail())
                .orElseThrow(InvalidLoginInformation::new);

        boolean matches = passwordEncoder.matches(serviceRequest.getPassword(), user.getPassword());
        if (!matches) {
            throw new InvalidLoginInformation();
        }

        return user;
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
