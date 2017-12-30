package model.asset_checker;

import model.ApiException;
import model.asset.account.Account;
import model.asset_checker.abstract_checker.AssetChecker;

import java.util.List;

public class OfflineAssetChecker extends AssetChecker {

    private List<Account> accountList;
    private String name;

    public OfflineAssetChecker(String name, List<Account> accounts) {
        this.name = name;
        this.accountList = accounts;
    }

    public String getName() {
        return name;
    }

    public List<Account> retrieveAccounts() throws ApiException {
        return accountList;
    }
}
