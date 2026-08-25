package sumit.ai.ai_engineering.user.Configuration.CustomUserDetails;

import java.util.Collection;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import sumit.ai.ai_engineering.user.Model.User;

@Component
public class CustomUserDetails implements UserDetails {

    private User user;

    public CustomUserDetails(User user){
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
            new SimpleGrantedAuthority(
                user.getRole().name()
            )
        );
    }

    @Override
    public @Nullable String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUserName();
    }
    
    public Long getId(){
        return user.getId();
    }

    public String getRole(){
        return user.getRole().toString();
    }

    public String getEmail(){
        return user.getEmail();
    }
}
