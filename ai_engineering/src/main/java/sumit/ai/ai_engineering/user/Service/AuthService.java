package sumit.ai.ai_engineering.user.Service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import sumit.ai.ai_engineering.ExceptionHandler.UserAlreadyExists;
import sumit.ai.ai_engineering.user.Configuration.CustomUserDetails.CustomUserDetails;
import sumit.ai.ai_engineering.user.DTO.Login.LoginRequest;
import sumit.ai.ai_engineering.user.DTO.Login.LoginResponse;
import sumit.ai.ai_engineering.user.DTO.SignUp.SignUpRequest;
import sumit.ai.ai_engineering.user.JWT.JwtService;
import sumit.ai.ai_engineering.user.Model.User;
import sumit.ai.ai_engineering.user.Repository.UserRepo;


@Service
public class AuthService {

    private final UserRepo repo;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    public AuthService(UserRepo repo, PasswordEncoder encoder, AuthenticationManager authManager, JwtService jwtService){
        this.encoder = encoder;
        this.repo = repo;
        this.authManager = authManager;
        this.jwtService = jwtService;
    }

    public LoginResponse signUp(SignUpRequest request){
        if(repo.existsByEmail(request.getEmail())){
            throw new UserAlreadyExists("Email Already Exists");
        }
        if(repo.existsByUserName(request.getUserName())){
            throw new UserAlreadyExists("UserName Already Taken");
        }

        User newUser = User.builder()
                .userName(request.getUserName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(encoder.encode(request.getPassword()))
                .build();
        repo.save(newUser);

        CustomUserDetails userDetails = new CustomUserDetails(newUser);
        return buildResponse(userDetails, jwtService.generateToken(userDetails));
    }

    public LoginResponse login(LoginRequest request){
        Authentication authentication = authManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getLogin(), request.getPassword())
        );
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return buildResponse(userDetails, jwtService.generateToken(userDetails));
    }

    private LoginResponse buildResponse(CustomUserDetails userDetails, String token){
        return LoginResponse.builder()
                .userId(userDetails.getId())
                .userName(userDetails.getUsername())
                .email(userDetails.getEmail())
                .role(userDetails.getRole())
                .token(token)
                .build();
    }

}
