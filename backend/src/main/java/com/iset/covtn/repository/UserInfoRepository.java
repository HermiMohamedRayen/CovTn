package com.iset.covtn.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iset.covtn.models.UserInfo;

public interface UserInfoRepository extends JpaRepository<UserInfo, String> {
     Optional<UserInfo> findByEmail(String email); // Use 'email' if that is the correct field for login

}
