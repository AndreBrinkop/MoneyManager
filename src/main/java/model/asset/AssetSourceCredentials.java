package model.asset;

public class AssetSourceCredentials {

    private final char[] type;
    private final char[] user;

    private final char[] key;
    private final char[] secret;

    public AssetSourceCredentials(char[] type, char[] user, char[] key, char[] secret) {
        this.type = type;
        this.user = user;
        this.key = key;
        this.secret = secret;
    }

    public AssetSourceCredentials(char[] type, char[] user, char[] key) {
        this.type = type;
        this.user = user;
        this.key = key;
        this.secret = null;
    }

    public AssetSourceCredentials(char[] type, char[] user) {
        this.type = type;
        this.user = user;
        this.key = null;
        this.secret = null;
    }

    public String getType() {
        return new String(type);
    }

    public String getUser() {
        return new String(user);
    }

    public String getKey() {
        return new String(key);
    }

    public String getSecret() {
        return new String(secret);
    }
}
