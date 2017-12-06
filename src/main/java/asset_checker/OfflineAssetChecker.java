package asset_checker;

import model.ApiException;
import model.AssetChecker;
import model.asset.Account;
import model.asset.BasicAccount;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class OfflineAssetChecker extends AssetChecker {

    private List<Account> accountList;
    private String name;

    public OfflineAssetChecker(String name, Account... accounts) {
        this.name = name;
        this.accountList = new LinkedList<>();
        this.accountList.addAll(Arrays.asList(accounts));
    }

    public OfflineAssetChecker(String name, Double euroValue) {
        this.name = name;
        this.accountList = new LinkedList<>();
        this.accountList.add(new BasicAccount(name, euroValue));
    }

    public String getName() {
        return name;
    }

    public List<Account> retrieveAccounts() throws ApiException {
        return accountList;
    }
}
