package jimuanco.jimslog.api.service.auth.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class TokenResponse {

    private String accessToken;

    @Builder
    private TokenResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}
