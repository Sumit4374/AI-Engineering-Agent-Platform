package com.ai_engineering.auth_service.Repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ai_engineering.auth_service.Model.User;

@Repository
public interface UserRepo extends JpaRepository<User,Long> {
    Optional<User> findById(Long id);
    Optional<User> findByUserName(String userName);
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailOrUserName(String email,String userName);

    Boolean existsByEmail(String email);
    Boolean existsByUserName(String userName);
}
