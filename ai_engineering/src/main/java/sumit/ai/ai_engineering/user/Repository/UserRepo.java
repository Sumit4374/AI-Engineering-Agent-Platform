package sumit.ai.ai_engineering.user.Repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import sumit.ai.ai_engineering.user.Model.User;


public interface UserRepo extends JpaRepository<User,Long> {
    Optional<User> findByUserName(String userName);
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailOrUserName(String email,String userName);

    boolean existsByEmail(String email);
    boolean existsByUserName(String userName);
    boolean existsByEmailOrUserName(String email, String userName);
}
