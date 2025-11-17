package com.iset.covtn.models;


import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
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

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
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
