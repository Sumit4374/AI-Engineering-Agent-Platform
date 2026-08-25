package sumit.ai.ai_engineering.user.DTO.Login;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    
    @NotBlank(message = "Username or Email is Required!")
    private String login;

    @NotBlank(message = "Password is Required!")
    private String password;
}
// 