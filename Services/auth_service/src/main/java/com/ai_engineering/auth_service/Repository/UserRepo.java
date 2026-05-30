package com.ai_engineering.auth_service.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ai_engineering.auth_service.Model.User;


@Repository
public interface UserRepo extends JpaRepository<User,Long> {}
