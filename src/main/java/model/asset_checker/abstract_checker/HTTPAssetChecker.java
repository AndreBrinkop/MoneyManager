package model.asset_checker.abstract_checker;

import model.ApiException;
import model.asset.AssetSourceCredentials;
import model.asset.account.Account;
import org.apache.http.client.fluent.Executor;

import java.util.List;


public abstract class HTTPAssetChecker extends AssetChecker {

    protected Executor executor;

    public HTTPAssetChecker(AssetSourceCredentials credentials) {
        super(credentials);
        this.executor = getExecutor();
    }

    public abstract String getName();

    protected abstract void login() throws ApiException;

    protected abstract void logout() throws ApiException;

    protected abstract List<Account> retrieveAssetsWithActiveSession() throws ApiException;

    public List<Account> retrieveAccounts() throws ApiException {
        login();
        List<Account> accounts = retrieveAssetsWithActiveSession();
        logout();
        return accounts;
    }

}
