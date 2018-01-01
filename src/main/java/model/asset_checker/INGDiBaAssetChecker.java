package model.asset_checker;

import model.asset_checker.abstract_checker.HBCIAssetChecker;

public class INGDiBaAssetChecker extends HBCIAssetChecker {

    @Override
    public String getName() {
        return "ING-DiBa";
    }

    @Override
    public HBCIAssetCheckerPassport fillPassport(HBCIAssetCheckerPassport passport) {
        passport.setHost("fints.ing-diba.de/fints/");
        passport.setBLZ("50010517");
        return passport;
    }

}

