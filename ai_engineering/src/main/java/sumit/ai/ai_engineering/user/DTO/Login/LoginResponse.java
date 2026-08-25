package sumit.ai.ai_engineering.user.DTO.Login;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private Long userId;
    private String userName;
    private String email;
    private String role;
    private String token;
}
