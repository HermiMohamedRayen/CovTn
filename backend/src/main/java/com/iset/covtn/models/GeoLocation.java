package com.iset.covtn.models;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeoLocation {
    private double latitude;
    private double longitude;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GeoLocation that)) return false;
        return Double.compare(latitude, that.latitude) == 0 && Double.compare(longitude, that.longitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }

    public boolean isComplete() {
        if(latitude < 0 || longitude < 0) return false;
        return true;
    }
}
