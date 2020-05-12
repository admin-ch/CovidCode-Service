package ch.admin.bag.covidcode.authcodegeneration.service;

public interface TokenProvider {

    String createToken( String onsetDate, String fake);
}
