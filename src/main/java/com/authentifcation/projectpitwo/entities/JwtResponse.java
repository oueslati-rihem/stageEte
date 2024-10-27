package com.authentifcation.projectpitwo.entities;

import lombok.Data;

@Data
public class JwtResponse {

    private User user;
    private String jwtToken;
    public JwtResponse(User user, String jwtToken) {
        this.user = user;
        this.jwtToken = jwtToken;
    }



}
