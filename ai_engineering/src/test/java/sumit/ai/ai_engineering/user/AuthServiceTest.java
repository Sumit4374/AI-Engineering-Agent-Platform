package sumit.ai.ai_engineering.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import sumit.ai.ai_engineering.ExceptionHandler.UserAlreadyExists;
import sumit.ai.ai_engineering.user.Configuration.CustomUserDetails.CustomUserDetails;
import sumit.ai.ai_engineering.user.DTO.Login.LoginRequest;
import sumit.ai.ai_engineering.user.DTO.Login.LoginResponse;
import sumit.ai.ai_engineering.user.DTO.SignUp.SignUpRequest;
import sumit.ai.ai_engineering.user.JWT.JwtService;
import sumit.ai.ai_engineering.user.Model.User;
import sumit.ai.ai_engineering.user.Model.Enum.Role;
import sumit.ai.ai_engineering.user.Repository.UserRepo;
import sumit.ai.ai_engineering.user.Service.AuthService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepo userRepo;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authManager;
    @Mock private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepo, passwordEncoder, authManager, jwtService);
    }

    // ---- signUp ----

    @Test
    void signUp_newUser_savesAndReturnsToken() {
        SignUpRequest req = new SignUpRequest();
        req.setUserName("alice");
        req.setEmail("alice@example.com");
        req.setPhoneNumber("9876543210");
        req.setPassword("secret");

        when(userRepo.existsByEmail("alice@example.com")).thenReturn(false);
        when(userRepo.existsByUserName("alice")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("hashed");

        User saved = User.builder()
                .id(1L).userName("alice").email("alice@example.com")
                .password("hashed").role(Role.USER).build();
        when(userRepo.save(any(User.class))).thenReturn(saved);

        CustomUserDetails details = new CustomUserDetails(saved);
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        LoginResponse response = authService.signUp(req);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUserName()).isEqualTo("alice");
        verify(userRepo).save(any(User.class));
    }

    @Test
    void signUp_existingEmail_throwsUserAlreadyExists() {
        SignUpRequest req = new SignUpRequest();
        req.setEmail("dup@example.com");
        req.setUserName("someone");

        when(userRepo.existsByEmail("dup@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.signUp(req))
                .isInstanceOf(UserAlreadyExists.class)
                .hasMessageContaining("Email Already Exists");

        verify(userRepo, never()).save(any());
    }

    @Test
    void signUp_existingUserName_throwsUserAlreadyExists() {
        SignUpRequest req = new SignUpRequest();
        req.setEmail("unique@example.com");
        req.setUserName("taken");

        when(userRepo.existsByEmail("unique@example.com")).thenReturn(false);
        when(userRepo.existsByUserName("taken")).thenReturn(true);

        assertThatThrownBy(() -> authService.signUp(req))
                .isInstanceOf(UserAlreadyExists.class)
                .hasMessageContaining("UserName Already Taken");

        verify(userRepo, never()).save(any());
    }

    // ---- login ----

    @Test
    void login_validCredentials_returnsToken() {
        LoginRequest req = new LoginRequest();
        req.setLogin("alice");
        req.setPassword("secret");

        User user = User.builder()
                .id(1L).userName("alice").email("alice@example.com")
                .password("hashed").role(Role.USER).build();
        CustomUserDetails details = new CustomUserDetails(user);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(details);
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(jwtService.generateToken(details)).thenReturn("jwt-token");

        LoginResponse response = authService.login(req);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getEmail()).isEqualTo("alice@example.com");
    }
}
