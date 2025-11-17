package com.iset.covtn.service;


import com.iset.covtn.models.Car;
import com.iset.covtn.models.UserInfo;
import com.iset.covtn.repository.UserInfoRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DriverService {


    private final UserInfoRepository userInfoRepository;

    public DriverService(UserInfoRepository userInfoRepository) {
        this.userInfoRepository = userInfoRepository;
    }

    public void setCar(String email, Car car) {
        Optional<UserInfo> userOpt = userInfoRepository.findById(email);
        if (userOpt.isPresent()) {
            UserInfo user = userOpt.get();
            user.setCar(car);
            userInfoRepository.save(user);
        }
    }



}
