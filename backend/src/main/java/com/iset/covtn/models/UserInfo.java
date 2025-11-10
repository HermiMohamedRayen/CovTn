package com.iset.covtn.models;

import java.util.HashSet;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {
    @Id
    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private HashSet<String> roles; // ROLE_PASSENGER , ROLE_ADMIN, ROLE_DRIVER
    private String profilePicture; // Store the filename or URL of the profile picture

     public void addRole(String role) {
        if (this.roles == null) {
            this.roles = new HashSet<>();
        }
        this.roles.add(role);
    }

    public String[] getRoles() {
        return roles.toArray(String[]::new);
    }
    public void setRoles(HashSet<String> roles) {
        this.roles = roles;
    }
}
