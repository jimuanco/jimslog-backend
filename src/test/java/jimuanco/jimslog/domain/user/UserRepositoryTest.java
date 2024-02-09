package jimuanco.jimslog.domain.user;

import jimuanco.jimslog.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private UserRepository userRepository;

    @DisplayName("이메일로 회원을 조회한다.")
    @Test
    void findByEmail() {
        // given
        String email = "jim@gmail.com";
        User user = User.builder()
                .name("이름")
                .email(email)
                .password("1234")
                .build();
        userRepository.save(user);
        
        // when
        User foundUser = userRepository.findByEmail(email).get();

        // then
        assertThat(foundUser.getName()).isEqualTo("이름");
    }
}