package jimuanco.jimslog.api.service.auth;

import jakarta.servlet.http.HttpServletResponse;
import jimuanco.jimslog.api.service.auth.request.LoginServiceRequest;
import jimuanco.jimslog.api.service.auth.request.SignupServiceRequest;
import jimuanco.jimslog.api.service.auth.response.TokenResponse;
import jimuanco.jimslog.domain.user.*;
import jimuanco.jimslog.exception.EmailAlreadyExists;
import jimuanco.jimslog.exception.InvalidLoginInformation;
import jimuanco.jimslog.exception.InvalidRefreshToken;
import jimuanco.jimslog.exception.UserNotFound;
import jimuanco.jimslog.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static java.time.LocalDateTime.now;

@Transactional(readOnly = true)
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private final String ADMIN_ID;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtils jwtUtils,
                       RefreshTokenRepository refreshTokenRepository,
                       RedisTemplate<String, String> redisTemplate,
                       @Value("${jimslog.admin}") String adminId) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.refreshTokenRepository = refreshTokenRepository;
        this.redisTemplate = redisTemplate;
        this.ADMIN_ID = adminId;
    }

    @Transactional
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

    public TokenResponse login(LoginServiceRequest serviceRequest,
                               HttpServletResponse response,
                               LocalDateTime expiryDate) {
        User user = authenticateEmailAndPassword(serviceRequest);

        String accessToken = jwtUtils.generateAccessToken(user);

        refreshTokenRepository.findByUserEmail(user.getEmail())
                .ifPresent(token -> refreshTokenRepository.delete(token));

        String newRefreshToken = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
                .refreshToken(newRefreshToken)
                .userEmail(user.getEmail())
                .build();
        refreshTokenRepository.save(refreshToken);

        addRefreshTokenInCookie(response, newRefreshToken, Duration.between(now(), expiryDate));

        return TokenResponse.builder()
                .accessToken(accessToken)
                .role(user.getRole())
                .build();
    }

    public TokenResponse refresh(String refreshToken,
                                 HttpServletResponse response,
                                 Long minimumExpiration,
                                 LocalDateTime expiryDate) {
        Long secondsToLive = redisTemplate.getExpire("refreshToken:" + refreshToken, TimeUnit.SECONDS);
        if (secondsToLive < 0) {
            throw new InvalidRefreshToken();
        }

        RefreshToken foundToken = refreshTokenRepository.findById(refreshToken)
                .orElseThrow(InvalidRefreshToken::new);
        User user = userRepository.findByEmail(foundToken.getUserEmail())
                .orElseThrow(UserNotFound::new);

        String accessToken = jwtUtils.generateAccessToken(user);

        if (secondsToLive < minimumExpiration) {
            refreshTokenRepository.delete(foundToken);

            String newRefreshToken = UUID.randomUUID().toString();
            RefreshToken refreshTokenToRedis = RefreshToken.builder()
                    .refreshToken(newRefreshToken)
                    .userEmail(user.getEmail())
                    .build();
            refreshTokenRepository.save(refreshTokenToRedis);

            addRefreshTokenInCookie(response, newRefreshToken, Duration.between(now(), expiryDate));
        }

        return TokenResponse.builder()
                .accessToken(accessToken)
                .role(user.getRole())
                .build();
    }

    @Transactional
    public void logout(String refreshToken, HttpServletResponse response) {
        RefreshToken foundToken = refreshTokenRepository.findById(refreshToken)
                .orElseThrow(InvalidRefreshToken::new);
        refreshTokenRepository.delete(foundToken);

        addRefreshTokenInCookie(response, refreshToken, Duration.ZERO);
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

    private void addRefreshTokenInCookie(HttpServletResponse response, String newToken, Duration duration) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", newToken)
                .maxAge(duration)
                .path("/")
                .secure(true)
                .sameSite("None")
                .httpOnly(true)
                .build();

        response.setHeader("Set-Cookie", cookie.toString());
    }
}
