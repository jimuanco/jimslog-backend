package jimuanco.jimslog.domain.user;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jimuanco.jimslog.domain.BaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
@Entity
public class RefreshToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    private String refreshToken;
    private String userEmail;

    private LocalDateTime expiryDate;

    @Builder
    private RefreshToken(String refreshToken, String userEmail, LocalDateTime expiryDate) {
        this.refreshToken = refreshToken;
        this.userEmail = userEmail;
        this.expiryDate = expiryDate;
    }

    public void updateToken(String newToken, LocalDateTime expiryDate) {
        this.refreshToken = newToken;
        this.expiryDate = expiryDate;
    }
}
