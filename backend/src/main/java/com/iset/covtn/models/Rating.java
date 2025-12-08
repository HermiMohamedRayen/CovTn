package com.iset.covtn.models;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String comment;
    private int rating;

    @JsonIgnoreProperties({
            "ratings",
            "ratingsSent",
            "car",
            "rideParticipations"
    })
    @ManyToOne
    private UserInfo user;

    @JsonIgnoreProperties({
            "ratings",
            "ratingsSent",
            "car",
            "rideParticipations"
    })
    @ManyToOne
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private UserInfo targetUser;

}
