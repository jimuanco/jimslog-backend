package jimuanco.jimslog.api.service.auth;

import jimuanco.jimslog.api.service.auth.request.SignupServiceRequest;
import jimuanco.jimslog.domain.user.User;
import jimuanco.jimslog.domain.user.UserRepository;
import jimuanco.jimslog.exception.EmailAlreadyExists;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
    void signup2() {
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
}