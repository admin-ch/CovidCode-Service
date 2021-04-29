package ch.admin.bag.covidcode.authcodegeneration.api;

import java.util.ArrayList;
import java.util.List;

public class AuthorizationCodeVerifyResponseDtoWrapper {

  private AuthorizationCodeVerifyResponseDto swissCovidAccessToken;
  private AuthorizationCodeVerifyResponseDto notifyMeAccessToken;

  public AuthorizationCodeVerifyResponseDtoWrapper(AuthorizationCodeVerifyResponseDto swissCovidAccessToken, AuthorizationCodeVerifyResponseDto notifyMeAccessToken) {
    this.swissCovidAccessToken = swissCovidAccessToken;
    this.notifyMeAccessToken = notifyMeAccessToken;
  }

  public AuthorizationCodeVerifyResponseDtoWrapper() {}


  public AuthorizationCodeVerifyResponseDto getSwissCovidAccessToken() {
    return swissCovidAccessToken;
  }

  public void setSwissCovidAccessToken(AuthorizationCodeVerifyResponseDto swissCovidAccessToken) {
    this.swissCovidAccessToken = swissCovidAccessToken;
  }

  public AuthorizationCodeVerifyResponseDto getNotifyMeAccessToken() {
    return notifyMeAccessToken;
  }

  public void setNotifyMeAccessToken(AuthorizationCodeVerifyResponseDto notifyMeAccessToken) {
    this.notifyMeAccessToken = notifyMeAccessToken;
  }
}
