package model.asset;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class AssetSourceCredentials {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long credentialsId;

    private char[] type;
    private char[] user;

    private char[] key;
    private char[] secret;

    public AssetSourceCredentials() {
    }

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

    public long getCredentialsId() {
        return credentialsId;
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
