package model;

public interface AssetChecker {

    String getName();

    AssetList retrieveAssets() throws ApiException;

}