package ch.admin.bag.covidcode.authcodegeneration.api;

public enum TokenType {
	
    DP3T_TOKEN("dp3t", "exposed"), CHECKIN_USERUPLOAD_TOKEN("checkin", "userupload");

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