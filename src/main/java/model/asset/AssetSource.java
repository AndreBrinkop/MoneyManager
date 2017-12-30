package model.asset;

import model.ApiException;
import model.asset.account.Account;
import model.asset_checker.abstract_checker.AssetChecker;

import java.math.BigDecimal;
import java.util.List;

import static util.NumberHelper.roundValue;

public class AssetSource {

    private String name;
    private List<Account> accounts;
    private AssetChecker checker;

    public AssetSource(AssetChecker checker) throws ApiException {
        this.name = checker.getName();
        this.accounts = checker.retrieveAccounts();
        this.checker = checker;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    // TODO: Replace
    public void removeAccount(Account account) {
        this.accounts.remove(account);
    }

    public BigDecimal getTotalEurValue() {
        return this.accounts.stream().map(Account::getCurrentBalance).map(Balance::getEuroBalanceValue).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void updateAssets() throws ApiException {
        this.accounts = this.checker.updateAssets(this.accounts);
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(name).append(": ").append(roundValue(getTotalEurValue())).append(" €\n");
        this.accounts.stream().forEach(account -> stringBuffer.append("\t").append(account.toString()).append("\n"));
        return stringBuffer.toString();
    }
}
