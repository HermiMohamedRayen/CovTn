package com.iset.covtn.models;

import java.util.Date;

import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ride {
    private Long id;
    private String departure;
    private String destination;
    private Date departureTime;
    private Date arrivalTime;

    @OneToMany
    private UserInfo driver;
}
