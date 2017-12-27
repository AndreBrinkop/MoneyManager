package model.asset;

public class AssetSourceCredentials {

    private final String type;
    private final String user;

    private final String key;
    private final String secret;

    public AssetSourceCredentials(String type, String user, String key, String secret) {
        this.type = type;
        this.user = user;
        this.key = key;
        this.secret = secret;
    }

    public AssetSourceCredentials(String type, String user, String key) {
        this.type = type;
        this.user = user;
        this.key = key;
        this.secret = null;
    }

    public AssetSourceCredentials(String type, String user) {
        this.type = type;
        this.user = user;
        this.key = null;
        this.secret = null;
    }

    public String getType() {
        return type;
    }

    public String getUser() {
        return user;
    }

    public String getKey() {
        return key;
    }

    public String getSecret() {
        return secret;
    }
}
