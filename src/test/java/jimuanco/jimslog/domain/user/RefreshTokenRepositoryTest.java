package jimuanco.jimslog.domain.user;

import jimuanco.jimslog.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @DisplayName("회원 이메일로 Refresh Token을 조회한다.")
    @Test
    void findByEmail() {
        // given
        String userEmail = "jim@gmail.com";
        String newToken = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
                .refreshToken(newToken)
                .userEmail(userEmail)
                .expiryDate(LocalDateTime.now().plusDays(30))
                .build();
        refreshTokenRepository.save(refreshToken);

        // when
        RefreshToken foundToken = refreshTokenRepository.findByUserEmail(userEmail).get();

        // then
        assertThat(foundToken.getRefreshToken()).isEqualTo(newToken);
    }

    @DisplayName("Refresh Token으로 회원 이메일을 조회한다.")
    @Test
    void findByRefreshToken() {
        // given
        String userEmail = "jim@gmail.com";
        String newToken = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
                .refreshToken(newToken)
                .userEmail(userEmail)
                .expiryDate(LocalDateTime.now().plusDays(30))
                .build();
        refreshTokenRepository.save(refreshToken);

        // when
        RefreshToken foundToken = refreshTokenRepository.findByRefreshToken(newToken).get();

        // then
        assertThat(foundToken.getUserEmail()).isEqualTo(userEmail);
    }
}