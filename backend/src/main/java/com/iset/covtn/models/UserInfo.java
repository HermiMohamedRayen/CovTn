package com.iset.covtn.models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "email")
public class UserInfo {
    @Id
    private String email;
    private String firstName;
    private String lastName;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private long number;
    private HashSet<String> roles; // ROLE_PASSENGER , ROLE_ADMIN, ROLE_DRIVER
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String profilePicture; // Store the filename or URL of the profile picture

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @OneToMany(fetch = FetchType.EAGER , mappedBy = "driver")
    private List<Ride> rides;

    @OneToOne(cascade = CascadeType.ALL)
    private Car car;

     public void addRole(String role) {
        if (this.roles == null) {
            this.roles = new HashSet<>();
        }
        this.roles.add(role);
    }

    public String[] getRoles() {
        return roles.toArray(String[]::new);
    }
}
