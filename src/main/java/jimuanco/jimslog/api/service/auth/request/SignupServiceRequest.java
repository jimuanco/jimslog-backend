package jimuanco.jimslog.api.service.auth.request;

import lombok.Builder;

public class SignupServiceRequest {

    private String name;
    private String email;
    private String password;

    @Builder
    private SignupServiceRequest(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }
}
