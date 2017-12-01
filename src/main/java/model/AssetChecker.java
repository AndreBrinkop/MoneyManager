package model;

import model.asset.Account;
import model.asset.AssetSource;

import java.util.List;

public abstract class AssetChecker {

    public abstract String getName();

    protected abstract List<Account> retrieveAssets() throws ApiException;

    public AssetSource retrieveAssetss() throws ApiException {
        return new AssetSource(getName(), retrieveAssets());
    }

}