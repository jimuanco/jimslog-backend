package jimuanco.jimslog.api.controller.auth.request;

import jakarta.validation.constraints.NotBlank;
import jimuanco.jimslog.api.service.auth.request.LoginServiceRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginRequest {

    @NotBlank(message = "이메일을 입력해주세요.")
    private String email;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;

    @Builder
    private LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public LoginServiceRequest toServiceRequest() {
        return LoginServiceRequest.builder()
                .email(email)
                .password(password)
                .build();
    }
}
