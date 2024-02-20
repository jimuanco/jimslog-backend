package jimuanco.jimslog.api.service.auth.response;

import jimuanco.jimslog.domain.user.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
public class TokenResponse {

    private String accessToken;
    private Role role;

    @Builder
    private TokenResponse(String accessToken, Role role) {
        this.accessToken = accessToken;
        this.role = role;
    }
}
