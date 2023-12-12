package jimuanco.jimslog.api.service.auth;

import jakarta.persistence.EntityManager;
import jimuanco.jimslog.api.service.auth.request.LoginServiceRequest;
import jimuanco.jimslog.api.service.auth.request.SignupServiceRequest;
import jimuanco.jimslog.api.service.auth.response.TokenResponse;
import jimuanco.jimslog.domain.user.RefreshToken;
import jimuanco.jimslog.domain.user.RefreshTokenRepository;
import jimuanco.jimslog.domain.user.User;
import jimuanco.jimslog.domain.user.UserRepository;
import jimuanco.jimslog.exception.EmailAlreadyExists;
import jimuanco.jimslog.exception.InvalidLoginInformation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private EntityManager em;
    
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

        // when
        TokenResponse tokenResponse = authService.login(serviceRequest, response);

        // then
        assertThat(tokenResponse.getAccessToken()).isNotEmpty();
        assertThat(response.getHeader("Set-Cookie")).isNotEmpty();
    }

    @DisplayName("로그인할 때마다 새로운 Refresh Token을 DB에 저장하고 반환한다")
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

        // when
        authService.login(serviceRequest, response);
        RefreshToken refreshToken1 = refreshTokenRepository.findByUserEmail("jim@gmail.com").get();
//        String token1 = refreshToken1.getRefreshToken();

        em.flush();
        em.clear();

        authService.login(serviceRequest, response);
        RefreshToken refreshToken2 = refreshTokenRepository.findByUserEmail("jim@gmail.com").get();
//        String token2 = refreshToken2.getRefreshToken();

        // then
        assertThat(refreshToken1.getRefreshToken()).isNotEqualTo(refreshToken2.getRefreshToken());
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

        // when // then
        assertThatThrownBy(() -> authService.login(serviceRequest, response))
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

        // when // then
        assertThatThrownBy(() -> authService.login(serviceRequest, response))
                .isInstanceOf(InvalidLoginInformation.class)
                .hasMessage("아이디/비밀번호가 올바르지 않습니다.");
    }
}