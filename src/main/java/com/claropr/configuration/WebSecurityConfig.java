package com.claropr.configuration;

import com.claropr.model.IccLov;
import com.claropr.security.JwtRequestFilter;
import com.claropr.security.TestUserDetailsService;
import com.claropr.security.TestUserPasswordEncoder;
import com.claropr.service.ICCService;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
  private final ICCService iccService;
  private final JwtRequestFilter jwtRequestFilter;
  private final TestUserDetailsService testUserDetailsService;
  private final TestUserPasswordEncoder testUserPasswordEncoder;

  public WebSecurityConfig(ICCService iccService, JwtRequestFilter jwtRequestFilter, TestUserDetailsService testUserDetailsService, TestUserPasswordEncoder testUserPasswordEncoder) {
    this.iccService = iccService;
    this.jwtRequestFilter = jwtRequestFilter;
    this.testUserDetailsService = testUserDetailsService;
    this.testUserPasswordEncoder = testUserPasswordEncoder;
  }

  @Override
  protected void configure(HttpSecurity httpSecurity) throws Exception {
    httpSecurity
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            .maximumSessions(2)
            .expiredUrl("/?sessionExpired");

    httpSecurity
            .csrf().ignoringAntMatchers("/contract-ws/**", "/api/**", "/contract/api/**");

    httpSecurity
            .authorizeRequests()
            .mvcMatchers("/contract/api/search/**").permitAll()
            .mvcMatchers(
                    "/resources/**",
                    "/contract-ws/**",
                    "/api/**",
                    "/contract/api/login-ws",
                    "/contract/api/logout-ws",
                    "/contract/api/auth/azure/**",
                    "/contract/api/test/**")
            .permitAll()
            .mvcMatchers("/contract/api/contracts/**").authenticated()
            .anyRequest()
            .authenticated();

    // No form login needed for API-only service
    httpSecurity.httpBasic().disable();
    httpSecurity.formLogin().disable();

    httpSecurity.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    // Temporarily disable LDAP to test if it's blocking the flow
    // this.configureLdapAuthentication(auth);
    this.configureInMemoryAuthentication(auth);
    // Configure database authentication for test users
    this.configureTestUserAuthentication(auth);
  }

  private void configureLdapAuthentication(AuthenticationManagerBuilder auth) throws Exception {
    IccLov ctxSourceLov = iccService.getIccLovByKey("LDAP_AUTH_CTX_SOURCE");

    String[] ctxSourceStr = ctxSourceLov.getLovDescription().split("@");

    auth.ldapAuthentication()
            .userSearchBase("DC=prt,DC=local")
            .userSearchFilter("sAMAccountName={0}")
            .groupSearchBase("OU=PRT Apps,DC=prt,DC=local")
            .groupSearchFilter("member={0}")
            .rolePrefix("")
            .userDetailsContextMapper(ldapUserDetailsMapper())
            .ldapAuthoritiesPopulator(ldapAuthoritiesPopulator())
            .contextSource()
            .url(ctxSourceStr[0])
            .port(Integer.parseInt(ctxSourceStr[1]))
            .managerDn(ctxSourceStr[2])
            .managerPassword(ctxSourceStr[3])
            .root("DC=prt,DC=local");
  }

  private void configureInMemoryAuthentication(AuthenticationManagerBuilder auth) throws Exception {
    auth.inMemoryAuthentication()
            .withUser("isy94545")
            .password("claro123")
            .authorities("IC_ADMIN", "POS_ADMIN");
  }

  private void configureTestUserAuthentication(AuthenticationManagerBuilder auth) throws Exception {
    // Configure authentication for test users using our custom UserDetailsService
    auth.userDetailsService(testUserDetailsService)
            .passwordEncoder(testUserPasswordEncoder);
  }

  private LdapAuthoritiesPopulator ldapAuthoritiesPopulator() {
    return new LdapAuthoritiesPopulator() {
      @Override
      public Collection<? extends GrantedAuthority> getGrantedAuthorities(
              DirContextOperations dirContextOperations, String s) {
        LinkedList<SimpleGrantedAuthority> list = new LinkedList<>();

        try {
          Arrays.stream(dirContextOperations.getObjectAttributes("memberOf"))
                  .iterator()
                  .forEachRemaining(
                          m -> {
                            if (m.toString().contains("OU=MAPA POS ICC")
                                || m.toString().contains("OU=ICC")) {
                              list.add(new SimpleGrantedAuthority(m.toString().split(",")[0].substring(3)));
                            }
                          });
        } catch (Exception e) {
          String safeSummary = buildSafeSummary(e);
          iccService.insertErrorInDB(
                  "System",
                  safeSummary,
                  null,
                  null,
                  "WebSecurityConfig:",
                  "ldapAuthoritiesPopulator(): Error 2015");

          list.add(new SimpleGrantedAuthority(""));
        }

        return list;
      }
    };
  }

  @Bean
  protected LdapUserDetailsMapper ldapUserDetailsMapper() {
    return new CustomLdapUserDetailsContextMapper();
  }

  @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }

  private static String buildSafeSummary(Throwable t) {
    String type = t.getClass().getName();
    String msg = t.getMessage() == null ? "" : redactSecrets(t.getMessage());
    if (msg.length() > 200) msg = msg.substring(0, 200) + "...";
    return type + (msg.isEmpty() ? "" : (": " + msg));
  }

  private static String redactSecrets(String s) {
    // oculta posibles secretos en el mensaje
    return s.replaceAll("(?i)(password|passwd|secret|token|apikey|key)=[^\\s&]+", "$1=REDACTED");
  }

}
