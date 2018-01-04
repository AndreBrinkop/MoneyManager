package model.asset;

import model.ApiException;
import model.asset.account.Account;
import model.asset_checker.abstract_checker.AssetChecker;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import static util.NumberHelper.roundValue;

@Entity
public class AssetSource implements AssetObject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long assetSourceId;

    protected String name;
    private Long credentialsId = null;

    @OneToMany(cascade = {CascadeType.ALL})
    protected List<Account> accounts;

    public AssetSource() {
    }

    public AssetSource(AssetSourceCredentials credentials) {
        this.name = AssetChecker.getAssetChecker(credentials).getName();
        this.credentialsId = credentials.getCredentialsId();
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public Balance getCurrentEurBalance() {
        return getCurrentEurBalance(null);
    }

    @Override
    public Balance getCurrentEurBalance(List<String> ignoredAccountNames) {
        if (ignoredAccountNames == null) {
            ignoredAccountNames = new LinkedList<>();
        }
        List<String> finalIgnoredAccountNames = ignoredAccountNames;
        return new Balance(this.accounts
                .stream()
                .filter(account -> !finalIgnoredAccountNames.contains(account.getName()))
                .map(Account::getCurrentEurBalance)
                .map(Balance::getEuroBalanceValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    public void updateAssets(AssetSourceCredentials credentials) throws ApiException {
        AssetChecker checker = AssetChecker.getAssetChecker(credentials);
        if (this.accounts == null) {
            this.accounts = checker.retrieveAccounts(credentials);
        } else {
            this.accounts = checker.updateAssets(credentials, this.accounts);
        }
    }

    public String getName() {
        return name;
    }

    public Long getCredentialsId() {
        return credentialsId;
    }

    @Override
    public String toString() {
        return toString(null);
    }

    public String toString(List<String> ignoredAccountNames) {
        if (ignoredAccountNames == null) {
            ignoredAccountNames = new LinkedList<>();
        }
        StringBuffer stringBuffer = new StringBuffer();

        BigDecimal totalEurValue = getCurrentEurBalance().getEuroBalanceValue();
        stringBuffer.append(name).append(": ").append(roundValue(totalEurValue)).append(" €\n");
        List<String> finalIgnoredAccountNames = ignoredAccountNames;
        this.accounts
                .stream()
                .filter(account -> !finalIgnoredAccountNames.contains(account.getName()))
                .forEach(account -> stringBuffer.append("\t").append(account.toString()).append("\n"));
        return stringBuffer.toString();
    }
}
