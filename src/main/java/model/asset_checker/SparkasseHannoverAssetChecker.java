package model.asset_checker;

import model.asset_checker.abstract_checker.HBCIAssetChecker;

public class SparkasseHannoverAssetChecker extends HBCIAssetChecker {

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
}

