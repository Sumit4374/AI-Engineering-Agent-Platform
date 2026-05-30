package com.ai_engineering.auth_service.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Users")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
public class User {
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column
    private String name;

    @Column(unique = true)
    private String userName;

    @NotNull
    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String phoneNumber;

    @NotNull
    private String password;

}
