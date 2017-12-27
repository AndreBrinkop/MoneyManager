package model.asset;

public class AssetSourceCredentials {

    private final String name;
    private final String user;

    private final String key;
    private final String secret;

    public AssetSourceCredentials(String name, String user, String key, String secret) {
        this.name = name;
        this.user = user;
        this.key = key;
        this.secret = secret;
    }

    public AssetSourceCredentials(String name, String user, String key) {
        this.name = name;
        this.user = user;
        this.key = key;
        this.secret = null;
    }

    public AssetSourceCredentials(String name, String user) {
        this.name = name;
        this.user = user;
        this.key = null;
        this.secret = null;
    }

    public String getName() {
        return name;
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
