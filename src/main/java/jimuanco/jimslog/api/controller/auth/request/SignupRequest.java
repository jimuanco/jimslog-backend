package jimuanco.jimslog.api.controller.auth.request;

import jakarta.validation.constraints.NotBlank;
import jimuanco.jimslog.api.service.auth.request.SignupServiceRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequest {

    @NotBlank(message = "이름을 입력해주세요.")
    private String name;

    @NotBlank(message = "이메일을 입력해주세요.")
    private String email;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;

    @Builder
    private SignupRequest(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public SignupServiceRequest toServiceRequest() {
        return SignupServiceRequest.builder()
                .name(name)
                .email(email)
                .password(password)
                .build();
    }
}
