package model.asset.account;

import model.asset.AssetSource;
import model.asset.AssetSourceCredentials;

import javax.persistence.Entity;
import java.util.List;

import static java.util.Arrays.asList;

@Entity
public class OfflineAssetSource extends AssetSource {

    public OfflineAssetSource() {
        this.name = "Offline Accounts";
    }

    public OfflineAssetSource(List<Account> accounts) {
        this();
        this.accounts = accounts;
    }

    public OfflineAssetSource(Account account) {
        this();
        this.accounts = asList(account);
    }

    public void updateAssets(AssetSourceCredentials credentials) {
        return;
    }

}
