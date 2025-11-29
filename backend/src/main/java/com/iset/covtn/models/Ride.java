package com.iset.covtn.models;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor


public class Ride {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private GeoLocation departure;
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "destination_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "destination_longitude"))
    })
    private GeoLocation destination;
    private Date departureTime;
    private Date arrivalTime;

    private boolean approved;

    @ManyToOne
    @NotNull
    private UserInfo driver;

    @OneToMany(fetch = FetchType.LAZY,mappedBy = "ride",cascade = CascadeType.REMOVE)
    private List<RideParticipation> rideParticipations;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Ride ride)) return false;
        return Objects.equals(id, ride.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }



}
