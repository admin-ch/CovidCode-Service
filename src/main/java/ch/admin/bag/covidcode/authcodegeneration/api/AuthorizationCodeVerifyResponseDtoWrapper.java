package ch.admin.bag.covidcode.authcodegeneration.api;

import java.util.ArrayList;
import java.util.List;

public class AuthorizationCodeVerifyResponseDtoWrapper {

  private List<AuthorizationCodeVerifyResponseDto> responseDtoList;

  public AuthorizationCodeVerifyResponseDtoWrapper(
      List<AuthorizationCodeVerifyResponseDto> responseDtoList) {
    this.responseDtoList = new ArrayList<>(responseDtoList);
  }

  public AuthorizationCodeVerifyResponseDtoWrapper() {
    responseDtoList = new ArrayList<>();
  }

  public List<AuthorizationCodeVerifyResponseDto> getResponseDtoList() {
    return new ArrayList<>(responseDtoList);
  }

  public void setResponseDtoList(List<AuthorizationCodeVerifyResponseDto> responseDtoList) {
    this.responseDtoList = responseDtoList;
  }
}
