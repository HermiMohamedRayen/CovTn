package com.iset.covtn.models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
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
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private String number;
    private HashSet<String> roles; // ROLE_PASSENGER , ROLE_ADMIN, ROLE_DRIVER
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String profilePicture; // Store the filename or URL of the profile picture

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @OneToMany(fetch = FetchType.LAZY , mappedBy = "driver", cascade = CascadeType.ALL)
    private List<Ride> rides;


    @OneToOne(cascade = CascadeType.ALL)
    private Car car;

    @JsonIgnore
    @OneToMany(mappedBy = "rider",fetch = FetchType.LAZY)
    private List<RideParticipation> rideParticipations;

     public void addRole(String role) {
        if (this.roles == null) {
            this.roles = new HashSet<>();
        }
        this.roles.add(role);
    }

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL,mappedBy = "user")
    private List<Rating> ratingsSent;

    @OneToMany(cascade = CascadeType.ALL,mappedBy = "targetUser")
    private List<Rating> ratings;


    public String[] getRoles() {
        return roles.toArray(String[]::new);
    }
}
