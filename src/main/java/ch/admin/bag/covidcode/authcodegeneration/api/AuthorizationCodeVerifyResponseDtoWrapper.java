package ch.admin.bag.covidcode.authcodegeneration.api;

public class AuthorizationCodeVerifyResponseDtoWrapper {

  private AuthorizationCodeVerifyResponseDto dp3tAccessToken;
  private AuthorizationCodeVerifyResponseDto checkInAccessToken;

  public AuthorizationCodeVerifyResponseDtoWrapper(AuthorizationCodeVerifyResponseDto dp3tAccessToken, AuthorizationCodeVerifyResponseDto checkInAccessToken) {
    this.dp3tAccessToken = dp3tAccessToken;
    this.checkInAccessToken = checkInAccessToken;
  }

  public AuthorizationCodeVerifyResponseDtoWrapper() {}


  public AuthorizationCodeVerifyResponseDto getDP3TAccessToken() {
    return dp3tAccessToken;
  }

  public void setDP3TAccessToken(AuthorizationCodeVerifyResponseDto dp3tAccessToken) {
    this.dp3tAccessToken = dp3tAccessToken;
  }

  public AuthorizationCodeVerifyResponseDto getCheckInAccessToken() {
    return checkInAccessToken;
  }

  public void setCheckInAccessToken(AuthorizationCodeVerifyResponseDto checkInAccessToken) {
    this.checkInAccessToken = checkInAccessToken;
  }
}
