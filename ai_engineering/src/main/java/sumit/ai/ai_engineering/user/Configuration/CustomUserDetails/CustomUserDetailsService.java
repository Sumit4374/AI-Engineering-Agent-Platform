package sumit.ai.ai_engineering.user.Configuration.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import sumit.ai.ai_engineering.user.Model.User;
import sumit.ai.ai_engineering.user.Repository.UserRepo;

@Service
public class CustomUserDetailsService implements UserDetailsService  {

    private UserRepo userRepo;

    public CustomUserDetailsService(UserRepo userRepo){
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        User user = userRepo.findByEmailOrUserName(login, login)
                            .orElseThrow(
                                () -> new UsernameNotFoundException("User not found")
                            );

        return new CustomUserDetails(user);
    }
    
}
