package asset_checker;

import asset_checker.abstract_checker.HBCIAssetChecker;

public class INGDiBaAssetChecker extends HBCIAssetChecker {

    public INGDiBaAssetChecker(String user, String password) {
        super(user, password);
    }

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

