package ch.admin.bag.covidcode.authcodegeneration.api;

public enum TokenType {
    SWISSCOVID_TOKEN("swissCovid", "exposed"), NOTIFYME_TOKEN("notifyMe", "tracekey");

    private final String scope;
    private final String audience;

    TokenType(String audience, String scope) {
        this.audience = audience;
        this.scope = scope;
    }

    public String getAudience() {
        return audience;
    }

    public String getScope() {
        return scope;
    }
}