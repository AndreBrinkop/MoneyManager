package model;

import model.asset.Account;
import model.asset.AssetSource;

import java.util.List;

public abstract class AssetChecker {

    public abstract String getName();

    protected abstract List<Account> retrieveAccounts() throws ApiException;

    public AssetSource retrieveAssets() throws ApiException {
        return new AssetSource(getName(), retrieveAccounts());
    }

}