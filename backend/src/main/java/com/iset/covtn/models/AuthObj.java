package com.iset.covtn.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthObj {
    private String email;
    private String id;
    private String code;
    private String token;
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        AuthObj authObj = (AuthObj) obj;

        if (!email.equals(authObj.email)) return false;
        if (!id.equals(authObj.id)) return false;
        return code.equals(authObj.code);
    }
}

