package com.claropr.configuration;

import com.claropr.model.LdapUser;
import java.util.Collection;
import javax.naming.directory.Attributes;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;

public class CustomLdapUserDetailsContextMapper extends LdapUserDetailsMapper {

  @Override
  public UserDetails mapUserFromContext(
      DirContextOperations ctx,
      String username,
      Collection<? extends GrantedAuthority> authorities) {
    LdapUser ldapUser = null;

    String userEmail;
    String givenName;
    String sn;

    Attributes attributes = ctx.getAttributes();

    UserDetails ldapUserDetails = super.mapUserFromContext(ctx, username, authorities);

    try {
      givenName = attributes.get("givenName").get().toString();
    } catch (javax.naming.NamingException | NullPointerException e) {
      givenName = "noname";
    }

    try {
      sn = attributes.get("sn").get().toString();
    } catch (javax.naming.NamingException | NullPointerException e) {
      sn = "person.sn cannot be null";
    }

    try {
      userEmail = attributes.get("mail").get().toString();
    } catch (javax.naming.NamingException | NullPointerException e) {
      userEmail = "noemail@claropr.com";
    }

    ldapUser = new LdapUser((LdapUserDetails) ldapUserDetails);
    ldapUser.setSn(sn);
    ldapUser.setGivenName(givenName);
    ldapUser.setUserEmail(userEmail);

    return (UserDetails) ldapUser;
  }

  @Override
  public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {}
}
