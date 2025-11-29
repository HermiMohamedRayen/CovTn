package com.iset.covtn.models;


import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String matriculationNumber;
    private String model;
    private ArrayList<String> photos;
    private boolean airConditioner;
    private boolean smoker;
    private int seats;

    @Transient
    private ArrayList<String> photosToRemove;

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,property = "email")
    @JsonIdentityReference(alwaysAsId = true)
    @OneToOne(fetch = FetchType.EAGER,mappedBy = "car")
    private UserInfo user;

    public boolean validCar(){
        if(matriculationNumber == null || matriculationNumber.isEmpty()){
            return false;
        } else if (model == null || model.isEmpty()) {
            return false;
        }else if( seats <= 0 || seats >= 9 ){
            return false;
        }
        return true;
    }
}
