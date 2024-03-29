package jimuanco.jimslog.api.service.auth;

import jakarta.persistence.EntityManager;
import jimuanco.jimslog.IntegrationTestSupport;
import jimuanco.jimslog.api.service.auth.request.LoginServiceRequest;
import jimuanco.jimslog.api.service.auth.request.SignupServiceRequest;
import jimuanco.jimslog.api.service.auth.response.TokenResponse;
import jimuanco.jimslog.domain.user.*;
import jimuanco.jimslog.exception.EmailAlreadyExists;
import jimuanco.jimslog.exception.InvalidLoginInformation;
import jimuanco.jimslog.exception.InvalidRefreshToken;
import jimuanco.jimslog.exception.UserNotFound;
import jimuanco.jimslog.utils.JwtUtils;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class AuthServiceTest extends IntegrationTestSupport {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private EntityManager em;

    String testSecretKey = "81b87ceccbd1c0d324168169b2292ab4b53bcd66b54f5e562d0fa4005ebd942f";

    @AfterEach
    public void tearDown() {
        refreshTokenRepository.deleteAll();
    }
    
    @DisplayName("회원가입을 할때 비밀번호는 암호화된다.")
    @Test
    void signup() {
        // given
        String rawPassword = "1234";

        SignupServiceRequest serviceRequest = SignupServiceRequest.builder()
                .name("이름")
                .email("jim@gmail.com")
                .password(rawPassword)
                .build();

        // when
        authService.signup(serviceRequest);
        
        // then
        User user = userRepository.findAll().get(0);
        assertThat(user.getPassword()).isNotEqualTo(rawPassword);
    }

    @DisplayName("회원가입을 할때 지정된 ADMIN 이메일로 가입하면 ADMIN Role이 부여된다.")
    @Test
    void signupWithAdminEmail() {
        // given
        String testAdminEmail = "admin@gmail.com";

        SignupServiceRequest serviceRequest = SignupServiceRequest.builder()
                .name("이름")
                .email(testAdminEmail)
                .password("1234")
                .build();

        AuthService testAuthService = new AuthService(
                userRepository,
                passwordEncoder,
                new JwtUtils(testSecretKey),
                refreshTokenRepository,
                redisTemplate,
                testAdminEmail
        );

        // when
        testAuthService.signup(serviceRequest);

        // then
        User user = userRepository.findAll().get(0);
        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
    }

    @DisplayName("회원가입을 할때 이미 가입된 이메일이 존재하면 예외가 발생한다.")
    @Test
    void signupWithExistingEmail() {
        // given
        String email = "jim@gmail.com";

        User user = User.builder()
                .name("이름")
                .email(email)
                .password("1234")
                .build();
        userRepository.save(user);

        SignupServiceRequest serviceRequest = SignupServiceRequest.builder()
                .name("이름2")
                .email(email)
                .password("12345")
                .build();

        // when // then
        assertThatThrownBy(() -> authService.signup(serviceRequest))
                .isInstanceOf(EmailAlreadyExists.class)
                .hasMessage("이미 가입된 이메일입니다.");
    }

    @DisplayName("로그인시 Refresh Token을 쿠키로 반환하고 Access Token을 응답값으로 반환한다.")
    @Test
    void login() {
        // given
        User user = User.builder()
                .name("이름")
                .email("jim@gmail.com")
                .password(passwordEncoder.encode("1234"))
                .build();
        userRepository.save(user);

        LoginServiceRequest serviceRequest = LoginServiceRequest.builder()
                .email("jim@gmail.com")
                .password("1234")
                .build();

        MockHttpServletResponse response = new MockHttpServletResponse();
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(30);

        // when
        TokenResponse tokenResponse = authService.login(serviceRequest, response, expiryDate);

        // then
        assertThat(tokenResponse.getAccessToken()).isNotEmpty();
        assertThat(response.getHeader("Set-Cookie")).isNotEmpty();
        assertThat(response.getCookie("refreshToken").getMaxAge()).isNotNull();
    }

    @DisplayName("로그인할 때마다 새로운 Refresh Token을 Redis에 저장하고 반환한다")
    @Test
    void loginTwice() {
        // given
        User user = User.builder()
                .name("이름")
                .email("jim@gmail.com")
                .password(passwordEncoder.encode("1234"))
                .build();
        userRepository.save(user);

        LoginServiceRequest serviceRequest = LoginServiceRequest.builder()
                .email("jim@gmail.com")
                .password("1234")
                .build();

        MockHttpServletResponse response = new MockHttpServletResponse();
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(30);

        // when
        authService.login(serviceRequest, response, expiryDate);
        RefreshToken refreshToken1 = refreshTokenRepository.findByUserEmail("jim@gmail.com").get();

        authService.login(serviceRequest, response, LocalDateTime.now().plusDays(30));
        RefreshToken refreshToken = refreshTokenRepository.findByUserEmail("jim@gmail.com").get();

        // then
        assertThat(refreshToken1.getRefreshToken()).isNotEqualTo(refreshToken.getRefreshToken());
    }

    @DisplayName("로그인할때 이메일이 맞지 않으면 예외가 발생한다.")
    @Test
    void loginWithWrongEmail() {
        // given
        User user = User.builder()
                .name("이름")
                .email("jim@gmail.com")
                .password(passwordEncoder.encode("1234"))
                .build();
        userRepository.save(user);

        LoginServiceRequest serviceRequest = LoginServiceRequest.builder()
                .email("jjim@gmail.com")
                .password("1234")
                .build();

        MockHttpServletResponse response = new MockHttpServletResponse();
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(30);

        // when // then
        assertThatThrownBy(() -> authService.login(serviceRequest, response, expiryDate))
                .isInstanceOf(InvalidLoginInformation.class)
                .hasMessage("아이디/비밀번호가 올바르지 않습니다.");
    }

    @DisplayName("로그인할때 비밀번호가 맞지 않으면 예외가 발생한다.")
    @Test
    void loginWithWrongPassword() {
        // given
        User user = User.builder()
                .name("이름")
                .email("jim@gmail.com")
                .password(passwordEncoder.encode("1234"))
                .build();
        userRepository.save(user);

        LoginServiceRequest serviceRequest = LoginServiceRequest.builder()
                .email("jjim@gmail.com")
                .password("12345")
                .build();

        MockHttpServletResponse response = new MockHttpServletResponse();
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(30);

        // when // then
        assertThatThrownBy(() -> authService.login(serviceRequest, response, expiryDate))
                .isInstanceOf(InvalidLoginInformation.class)
                .hasMessage("아이디/비밀번호가 올바르지 않습니다.");
    }

    @DisplayName("Refreseh Token으로 AccessToken을 재발급 받는다.")
    @Test
    void refresh() {
        // given
        User user = User.builder()
                .name("이름")
                .email("jim@gmail.com")
                .password(passwordEncoder.encode("1234"))
                .build();
        userRepository.save(user);

        LoginServiceRequest serviceRequest = LoginServiceRequest.builder()
                .email("jim@gmail.com")
                .password("1234")
                .build();
        MockHttpServletResponse response = new MockHttpServletResponse();

        authService.login(serviceRequest, response, LocalDateTime.now().plusDays(30));

        String refreshToken = response.getCookie("refreshToken").getValue().toString();
        Long minimumExpiration = 60 * 60 * 24 * 7L;
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(30);

        // when
        TokenResponse tokenResponse = authService.refresh(refreshToken, response, minimumExpiration, expiryDate);

        // then
        // JWT 생성시간, 만료시간이 비슷할 경우 같은 JWT가 만들어질 수도 있는 듯
        assertThat(tokenResponse.getAccessToken()).isNotEmpty();
    }

    @DisplayName("잘못된 Refresh Token으로 Access Token 재발급을 요청하면 예외가 발생한다.")
    @Test
    void refreshWithWrongRefreshToken() {
        // given
        User user = User.builder()
                .name("이름")
                .email("jim@gmail.com")
                .password(passwordEncoder.encode("1234"))
                .build();
        userRepository.save(user);

        LoginServiceRequest serviceRequest = LoginServiceRequest.builder()
                .email("jim@gmail.com")
                .password("1234")
                .build();
        MockHttpServletResponse response = new MockHttpServletResponse();
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(30);

        authService.login(serviceRequest, response, expiryDate);

        String WrongRefreshToken = response.getCookie("refreshToken").getValue().toString() + "a";
        Long minimumExpiration = 60 * 60 * 24 * 7L;

        // when // then
        assertThatThrownBy(() -> authService.refresh(WrongRefreshToken, response, minimumExpiration, expiryDate))
                .isInstanceOf(InvalidRefreshToken.class)
                .hasMessage("Refresh Token이 유효하지 않습니다.");

    }

    @DisplayName("만료된 Refresh Token으로 Access Token 재발급을 요청하면 만료된 토큰은 삭제되고 예외가 발생한다.")
    @Test
    void refreshWithExpiredRefreshToken() {
        // given
        String expiredRefreshToken = UUID.randomUUID().toString();
        RefreshToken refreshTokenOb = RefreshToken.builder()
                .refreshToken(expiredRefreshToken)
                .userEmail("jim@gmail.com")
                .expiration(1)
                .build();
        refreshTokenRepository.save(refreshTokenOb);

        MockHttpServletResponse response = new MockHttpServletResponse();
        Long minimumExpiration = 60 * 60 * 24 * 7L;
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(30);

        // when // then
        assertThatThrownBy(() ->
                Awaitility.await()
                        .atMost(2, TimeUnit.SECONDS)
                        .until(() -> !refreshTokenRepository.findById(expiredRefreshToken).get().getRefreshToken().equals(expiredRefreshToken)))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("No value present");

        assertThatThrownBy(() -> authService.refresh(expiredRefreshToken, response, minimumExpiration, expiryDate))
                .isInstanceOf(InvalidRefreshToken.class)
                .hasMessage("Refresh Token이 유효하지 않습니다.");

    }

    @DisplayName("Refresh Token의 주인이 존재 하지 않을경우 예외가 발생한다.")
    @Test
    void refreshWhenUserNotFoundByRefreshToken() {
        // given
        User user = User.builder()
                .name("이름")
                .email("jim@gmail.com")
                .password(passwordEncoder.encode("1234"))
                .build();
        userRepository.save(user);

        LoginServiceRequest serviceRequest = LoginServiceRequest.builder()
                .email("jim@gmail.com")
                .password("1234")
                .build();
        MockHttpServletResponse response = new MockHttpServletResponse();
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(30);

        authService.login(serviceRequest, response, expiryDate);

        String refreshToken = response.getCookie("refreshToken").getValue().toString();
        Long minimumExpiration = 60 * 60 * 24 * 7L;

        userRepository.delete(user);

        // when // then
        assertThatThrownBy(() -> authService.refresh(refreshToken, response, minimumExpiration, expiryDate))
                .isInstanceOf(UserNotFound.class)
                .hasMessage("존재하지 않는 사용자입니다.");

    }

    @DisplayName("Refreseh Token으로 AccessToken을 재발급 받을때 " +
            "Refresh Token의 만료 시간이 설정한 최소 기한 보다 짧게 남았으면 Refresh Token도 재발급 해준다.")
    @Test
    void refreshWhenRefreshTokenExpiresSoon() {
        // given
        User user = User.builder()
                .name("이름")
                .email("jim@gmail.com")
                .password(passwordEncoder.encode("1234"))
                .build();
        userRepository.save(user);

        LoginServiceRequest serviceRequest = LoginServiceRequest.builder()
                .email("jim@gmail.com")
                .password("1234")
                .build();
        MockHttpServletResponse response = new MockHttpServletResponse();
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(30);

        authService.login(serviceRequest, response, expiryDate);

        String refreshToken = response.getCookie("refreshToken").getValue().toString();
        Long minimumExpiration = 60 * 60 * 24 * 30L;

        // when
        TokenResponse tokenResponse = authService.refresh(refreshToken, response, minimumExpiration, expiryDate);

        // then
        String newRefreshToken = response.getCookie("refreshToken").getValue().toString();
        assertThat(tokenResponse.getAccessToken()).isNotEmpty();
        assertThat(refreshToken).isNotEqualTo(newRefreshToken);
    }

    @DisplayName("로그아웃한다.")
    @Test
    void logout() {
        // given
        String refreshToken = UUID.randomUUID().toString();
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .userEmail("jim@gmail.com")
                .refreshToken(refreshToken)
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        authService.logout(refreshToken, response);

        // then
        assertThat(response.getCookie("refreshToken").getMaxAge()).isZero();
        assertThat(refreshTokenRepository.findById(refreshToken).isEmpty()).isTrue();
    }

    @DisplayName("로그아웃시 잘못된 Refresh Token으로 요청하면 예외가 발생한다.")
    @Test
    void logoutWithWrongRefreshToken() {
        // given
        String refreshToken = UUID.randomUUID().toString();
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .userEmail("jim@gmail.com")
                .refreshToken(refreshToken)
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        MockHttpServletResponse response = new MockHttpServletResponse();

        // when // then
        assertThatThrownBy(() -> authService.logout(refreshToken + "a", response))
                .isInstanceOf(InvalidRefreshToken.class)
                .hasMessage("Refresh Token이 유효하지 않습니다.");
    }
}