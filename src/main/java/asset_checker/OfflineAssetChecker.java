package asset_checker;

import model.ApiException;
import model.Asset;
import model.AssetChecker;
import model.AssetList;

import java.util.Arrays;

public class OfflineAssetChecker implements AssetChecker {

    private AssetList assetList;
    private String name;

    public OfflineAssetChecker(String name, Asset... assets) {
        this.assetList = new AssetList(name);
        this.name = name;
        this.assetList.addAll(Arrays.asList(assets));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public AssetList retrieveAssets() throws ApiException {
        return assetList;
    }
}
