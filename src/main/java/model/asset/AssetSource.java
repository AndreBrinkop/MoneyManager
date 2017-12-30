package model.asset;

import model.asset.account.Account;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import static util.NumberHelper.roundValue;

public class AssetSource {

    private String name;
    private List<Account> accounts;

    public AssetSource(String name, List<Account> accounts) {
        this.name = name;
        this.accounts = accounts;
    }

    public AssetSource(String name) {
        this.name = name;
        this.accounts = new LinkedList<>();
    }

    public void addAccount(Account account) {
        this.accounts.add(account);
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void removeAccount(Account account) {
        this.accounts.remove(account);
    }

    public BigDecimal getTotalEurValue() {
        return this.accounts.stream().map(Account::getCurrentBalance).map(Balance::getEuroBalanceValue).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(name).append(": ").append(roundValue(getTotalEurValue())).append(" €\n");
        this.accounts.stream().forEach(account -> stringBuffer.append("\t").append(account.toString()).append("\n"));
        return stringBuffer.toString();
    }
}
