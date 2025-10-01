package com.claropr.security;

import com.claropr.dao.IccUserDao;
import com.claropr.model.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TestUserDetailsService implements UserDetailsService {

    private final IccUserDao iccUserDao;
    private final Map<String, String> testUsers = new HashMap<>();

    @Autowired
    public TestUserDetailsService(IccUserDao iccUserDao) {
        this.iccUserDao = iccUserDao;

        initializeTestUsers();
    }

    private void initializeTestUsers() {
        String pw = "claro123";
        testUsers.put("train_ic_user", pw);
        testUsers.put("train_ic_admin", pw);
        testUsers.put("train_ic_pos_user", pw);
        testUsers.put("train_ic_pos_admin", pw);
        testUsers.put("train_pos_agent", pw);
        testUsers.put("train_pos_user01", pw);
        testUsers.put("train_pos_user02", pw);
        testUsers.put("train_pos_user03", pw);
        testUsers.put("train_pos_loc_adm01", pw);
        testUsers.put("train_pos_loc_adm02", pw);
        testUsers.put("train_pos_loc_adm03", pw);
        testUsers.put("train_pos_loc_asst01", pw);
        testUsers.put("train_pos_loc_asst02", pw);
        testUsers.put("train_pos_loc_asst03", pw);
        testUsers.put("train_pos_admin", pw);

    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        if (testUsers.containsKey(username.toLowerCase())) {

            String password = testUsers.get(username.toLowerCase());

            // Try to get user from database
            List<Users> users = iccUserDao.getUserByUsername(username);

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            
            if (users.isEmpty()) {
                System.out.println("TestUserDetailsService: User not found in database, using default roles for: " + username);
                // Si no hay BD o usuario no existe, usar roles por defecto
                if (username.toLowerCase().contains("admin")) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                } else {
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                }
            } else {
                Users user = users.get(0);

                if (user.getEXPIRATION_DATE() != null) {
                    System.out.println("TestUserDetailsService: User account has expired: " + username);
                    throw new UsernameNotFoundException("User account has expired");
                }

                // Get authorities from LDAP_PROFILES field
                if (user.getLDAP_PROFILES() != null && !user.getLDAP_PROFILES().isEmpty()) {
                    String[] profiles = user.getLDAP_PROFILES().split(",");
                    for (String profile : profiles) {
                        authorities.add(new SimpleGrantedAuthority(profile.trim()));
                    }
                } else {
                    // Si no hay perfiles LDAP, usar roles por defecto
                    if (username.toLowerCase().contains("admin")) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                    } else {
                        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                    }
                }
            }

            return new User(username, password, authorities);
        }
        System.out.println("TestUserDetailsService:User not found or not a test user: " + username);
        throw new UsernameNotFoundException("User not found or not a test user: " + username);
    }
}
