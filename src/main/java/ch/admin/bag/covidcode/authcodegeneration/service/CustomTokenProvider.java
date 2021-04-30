package ch.admin.bag.covidcode.authcodegeneration.service;

import ch.admin.bag.covidcode.authcodegeneration.api.TokenType;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;
import java.util.UUID;

import static ch.admin.bag.covidcode.authcodegeneration.api.TokenType.DP3T_TOKEN;

@Component
@Slf4j
public class CustomTokenProvider {

  @Value("${authcodegeneration.jwt.token-validity}")
  private long tokenValidity;

  @Value("${authcodegeneration.jwt.issuer}")
  private String issuer;

  @Value("${authcodegeneration.jwt.privateKey}")
  private String privateKey;

  private KeyFactory rsa;

  @PostConstruct
  public void init() throws NoSuchAlgorithmException {
    rsa = KeyFactory.getInstance("RSA");
  }

  public String createToken(String onsetDate, String fake) {
    return createToken(onsetDate, fake, DP3T_TOKEN);
  }

  public String createToken(String onsetDate, String fake, TokenType tokenType) {
    final long nowMillis = System.currentTimeMillis();
    final Date now = new Date(nowMillis);

    final PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(Decoders.BASE64.decode(privateKey));
    final Key signingKey;

    try {
      signingKey = rsa.generatePrivate(spec);
    } catch (InvalidKeySpecException e) {
      log.error("Error during generate private key", e);
      throw new IllegalStateException(e);
    }

    return Jwts.builder()
        .setId(UUID.randomUUID().toString())
        .setIssuer(issuer)
        .setIssuedAt(now)
        .setNotBefore(now)
        .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
        .claim("aud", tokenType.getAudience())
        .claim("scope", tokenType.getScope())
        .claim("fake", fake)
        .claim("onset", onsetDate)
        .signWith(signingKey)
        .setExpiration(new Date(nowMillis + tokenValidity))
        .compact();
  }
}
