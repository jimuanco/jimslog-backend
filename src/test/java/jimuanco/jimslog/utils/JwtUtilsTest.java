package jimuanco.jimslog.utils;

import jimuanco.jimslog.domain.user.Role;
import jimuanco.jimslog.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilsTest {

    String testSecretKey = "81b87ceccbd1c0d324168169b2292ab4b53bcd66b54f5e562d0fa4005ebd942f";
    
    @DisplayName("JWT가 유효하면 true를 반환한다.")
    @Test
    void isTokenValid() {
        // give
        User user = User.builder()
                .email("jim@gmail.com")
                .role(Role.USER)
                .build();

        JwtUtils jwtUtils = new JwtUtils(testSecretKey);
        String accessToken = jwtUtils.generateAccessToken(user);

        // when
        boolean tokenValid = jwtUtils.isTokenValid(accessToken);

        // then
        assertThat(tokenValid).isTrue();

    }

    @DisplayName("JWT가 유효하지 않으면 false를 반환한다.")
    @Test
    void isTokenInvalid() {
        // given
        User user = User.builder()
                .email("jim@gmail.com")
                .role(Role.USER)
                .build();

        JwtUtils jwtUtils = new JwtUtils(testSecretKey);
        String accessToken = jwtUtils.generateAccessToken(user);

        // when
        boolean tokenValid = jwtUtils.isTokenValid(accessToken + "a");

        // then
        assertThat(tokenValid).isFalse();

    }

    @DisplayName("JWT에서 이메일을 추출한다.")
    @Test
    void getEmailFromToken() {
        // given
        String email = "jim@gmail.com";

        User user = User.builder()
                .email(email)
                .role(Role.USER)
                .build();

        JwtUtils jwtUtils = new JwtUtils(testSecretKey);
        String accessToken = jwtUtils.generateAccessToken(user);

        // when
        String parsedEmail = jwtUtils.getEmailFromToken(accessToken);

        // then
        assertThat(parsedEmail).isEqualTo(email);

    }

    @DisplayName("JWT에서 Role을 추출한다.")
    @Test
    void getRoleFromToken() {
        // given
        Role role = Role.USER;

        User user = User.builder()
                .email("jim@gmail.com")
                .role(role)
                .build();

        JwtUtils jwtUtils = new JwtUtils(testSecretKey);
        String accessToken = jwtUtils.generateAccessToken(user);

        // when
        String parsedRole = jwtUtils.getRoleFromToken(accessToken);

        // then
        assertThat(parsedRole).isEqualTo(role.toString());
    }

}