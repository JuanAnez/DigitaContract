package com.claropr.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginRequestData {
  private String username;
  private String password;
  private String appVersion;

  public LoginRequestData() {}

  public LoginRequestData(String username, String password) {
    this.username = username;
    this.password = password;
  }

  public LoginRequestData(String username, String password, String appVersion) {
    this.username = username;
    this.password = password;
    this.appVersion = appVersion;
  }
}

