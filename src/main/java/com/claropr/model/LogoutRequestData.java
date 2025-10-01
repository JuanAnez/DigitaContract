package com.claropr.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LogoutRequestData {
  private String accessToken;

  public LogoutRequestData() {}

  public LogoutRequestData(String accessToken) {
    this.accessToken = accessToken;
  }
}

