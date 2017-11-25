package asset_checker;

import asset_checker.abstract_checker.HBCIAssetChecker;
import model.ApiException;
import model.AssetList;

public class SparkasseHannoverAssetChecker extends HBCIAssetChecker {

    public SparkasseHannoverAssetChecker(String user, String password) {
        super(user, password);
    }

    @Override
    public String getName() {
        return "Sparkasse Hannover";
    }

    @Override
    public HBCIAssetCheckerPassport fillPassport(HBCIAssetCheckerPassport passport) {
        passport.setHost("banking-ni4.s-fints-pt-ni.de/fints30");
        passport.setBLZ("25050180");
        return passport;
    }

    @Override
    public AssetList retrieveAssets() throws ApiException {
        AssetList assetList = super.retrieveAssets();
        assetList.forEach(asset -> {
            if (asset.getDescription().contains("BonusSparen") || asset.getDescription().contains("KlassikSparen")) {
                asset.setShowAsset(false);
            }
        });
        return assetList;
    }
}

