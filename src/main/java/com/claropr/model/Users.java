package com.claropr.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class Users {
    private String USERNAME;
    private String NAME;
    private String LAST_NAME;
    private String USER_EMAIL;
    private Integer GROUP_ID;
    private Integer PROFILE_ID;
    private String LDAP_PROFILES;
    private Date EXPIRATION_DATE;

    public Users() {}
}

