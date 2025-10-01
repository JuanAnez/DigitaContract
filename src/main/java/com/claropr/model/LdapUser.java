package com.claropr.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.ldap.userdetails.LdapUserDetails;

@Setter
@Getter
public class LdapUser {
    private LdapUserDetails ldapUserDetails;
    private String sn;
    private String givenName;
    private String userEmail;

    public LdapUser() {}

    public LdapUser(LdapUserDetails ldapUserDetails) {
        this.ldapUserDetails = ldapUserDetails;
    }

    public String getUsername() {
        return ldapUserDetails.getUsername();
    }

    public String getPassword() {
        return ldapUserDetails.getPassword();
    }
}

