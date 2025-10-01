package com.claropr.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class AccessTokenData {
    private String token;
    private String username;
    private Date createdDate;
    private Date expirationDate;
    private boolean valid;

    public AccessTokenData() {}

    public AccessTokenData(String token, String username, Date createdDate, Date expirationDate, boolean valid) {
        this.token = token;
        this.username = username;
        this.createdDate = createdDate;
        this.expirationDate = expirationDate;
        this.valid = valid;
    }

    public boolean isValidToken() {
        return valid && expirationDate != null && expirationDate.after(new Date());
    }
}

