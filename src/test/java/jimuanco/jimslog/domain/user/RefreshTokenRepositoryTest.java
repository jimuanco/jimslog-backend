package jimuanco.jimslog.domain.user;

import jimuanco.jimslog.IntegrationTestSupport;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RefreshTokenRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @AfterEach
    public void tearDown() {
        refreshTokenRepository.deleteAll();
    }

    @DisplayName("Redis에 저장된 Refresh Token 객체를 refreshToken 필드값(UUID)으로 조회한다.")
    @Test
    void getRefreshTokenByTokenField() {
        // given
        String refreshToken = UUID.randomUUID().toString();
        RefreshToken refreshTokenOb = RefreshToken.builder()
                .refreshToken(refreshToken)
                .userEmail("jim@gmail.com")
                .build();
        // when
        refreshTokenRepository.save(refreshTokenOb);

        // then
        assertThat(refreshTokenRepository.count()).isEqualTo(1);
        assertThat(refreshTokenRepository.findById(refreshToken).get().getRefreshToken())
                .isEqualTo(refreshToken);
    }

    @DisplayName("Redis에 저장된 Refresh Token 객체를 userEmail 필드값으로 조회한다.")
    @Test
    void getRefreshTokenByUserEmailField() {
        // given
        String refreshToken = UUID.randomUUID().toString();
        String userEmail = "jim@gmail.com";
        RefreshToken refreshTokenOb = RefreshToken.builder()
                .refreshToken(refreshToken)
                .userEmail(userEmail)
                .build();
        // when
        refreshTokenRepository.save(refreshTokenOb);

        // then
        assertThat(refreshTokenRepository.count()).isEqualTo(1);
        assertThat(refreshTokenRepository.findByUserEmail(userEmail).get().getRefreshToken())
                .isEqualTo(refreshToken);
    }

    @DisplayName("Refresh Token 객체에 expiration을 지정하지 않으면 @RedisHash의 timeToLive값으로 유효기간이 지정된다.")
    @Test
    void saveRefreshTokenWithoutExpirationField() {
        // given
        String refreshToken = UUID.randomUUID().toString();
        RefreshToken refreshTokenOb = RefreshToken.builder()
                .refreshToken(refreshToken)
                .userEmail("jim@gmail.com")
                .build();
        // when
        refreshTokenRepository.save(refreshTokenOb);

        // then
        assertThat(redisTemplate.getExpire("refreshToken:" + refreshToken))
                .isEqualTo(2592000L);
    }

    @DisplayName("Redis에 저장된 Refresh Token은 유효기간이 지난 후 조회시 예외가 발생한다.")
    @Test
    void getRefreshTokenAfterTokenExpire() {
        // given
        String refreshToken = UUID.randomUUID().toString();
        String userEmail = "jim@gmail.com";
        RefreshToken refreshTokenOb = RefreshToken.builder()
                .refreshToken(refreshToken)
                .userEmail(userEmail)
                .expiration(1)
                .build();
        // when
        refreshTokenRepository.save(refreshTokenOb);

        // then
        assertThatThrownBy(() ->
                Awaitility.await()
                        .atMost(2, TimeUnit.SECONDS)
                        .until(() -> !refreshTokenRepository.findById(refreshToken).get()
                                .getRefreshToken().equals(refreshToken)))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("No value present");

        assertThatThrownBy(() -> refreshTokenRepository.findByUserEmail(userEmail).get())
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("No value present");
    }

    @DisplayName("Redis에 저장된 Refresh Token의 유효기간이 지난후 Expire 조회시 -2가 반환된다.")
    @Test
    void getRefreshToKenExpireAfterTokenExpire() {
        // given
        String refreshToken = UUID.randomUUID().toString();
        RefreshToken refreshTokenOb = RefreshToken.builder()
                .refreshToken(refreshToken)
                .userEmail("jim@gmail.com")
                .expiration(1)
                .build();
        // when
        refreshTokenRepository.save(refreshTokenOb);

        // then
        Awaitility.await()
                .atMost(2, TimeUnit.SECONDS)
                .until(() -> redisTemplate.getExpire("refreshToken:" + refreshToken) < 0);
        assertThat(redisTemplate.getExpire("refreshToken:" + refreshToken))
                .isEqualTo(-2L);
    }
}