package jimuanco.jimslog.api.service.auth.request;

import lombok.Builder;
import lombok.Getter;

@Getter
public class LoginServiceRequest {

    private String email;
    private String password;

    @Builder
    private LoginServiceRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
