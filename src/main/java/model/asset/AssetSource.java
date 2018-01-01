package model.asset;

import model.ApiException;
import model.asset.account.Account;
import model.asset_checker.OfflineAssetChecker;
import model.asset_checker.abstract_checker.AssetChecker;

import java.math.BigDecimal;
import java.util.List;

import static util.NumberHelper.roundValue;

public class AssetSource implements AssetObject {

    private String name;
    private List<Account> accounts;
    private AssetChecker checker;

    public AssetSource(AssetSourceCredentials credentials) {
        this.checker = AssetChecker.createAssetChecker(credentials);
        this.name = checker.getName();
    }

    public AssetSource(OfflineAssetChecker offlineAssetChecker) {
        this.checker = offlineAssetChecker;
        this.name = checker.getName();
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    // TODO: Replace
    public void removeAccount(Account account) {
        this.accounts.remove(account);
    }

    public Balance getCurrentEurBalance() {
        return new Balance(this.accounts.stream().map(Account::getCurrentEurBalance).map(Balance::getEuroBalanceValue).reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    public void updateAssets(AssetSourceCredentials credentials) throws ApiException {
        if (this.accounts == null) {
            this.accounts = this.checker.retrieveAccounts(credentials);
        } else {
            this.accounts = this.checker.updateAssets(credentials, this.accounts);
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();

        BigDecimal totalEurValue = getCurrentEurBalance().getEuroBalanceValue();
        stringBuffer.append(name).append(": ").append(roundValue(totalEurValue)).append(" €\n");
        this.accounts.stream().forEach(account -> stringBuffer.append("\t").append(account.toString()).append("\n"));
        return stringBuffer.toString();
    }
}
