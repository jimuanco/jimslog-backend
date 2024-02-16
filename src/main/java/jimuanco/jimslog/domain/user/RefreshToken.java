package jimuanco.jimslog.domain.user;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@RedisHash(value = "refreshToken", timeToLive = 60 * 60 * 24 * 30)
public class RefreshToken {

    @Id
    private String refreshToken;

    @Indexed
    private String userEmail;

    @TimeToLive
    Integer expiration;

    @Builder
    private RefreshToken(String refreshToken, String userEmail, Integer expiration) {
        this.refreshToken = refreshToken;
        this.userEmail = userEmail;
        this.expiration = expiration;
    }

    public void updateToken(String newToken) {
        this.refreshToken = newToken;
    }
}
