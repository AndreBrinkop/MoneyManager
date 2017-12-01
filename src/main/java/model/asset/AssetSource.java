package model.asset;

import java.util.LinkedList;
import java.util.List;

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

    public double getTotalEurValue() {
        return this.accounts.stream().mapToDouble(Account::getTotalEurValue).sum();
    }

    @Override
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(name).append(": ").append(getTotalEurValue()).append(" €\n");
        this.accounts.stream().forEach(account -> stringBuffer.append("\t").append(account.toString()).append("\n"));
        return stringBuffer.toString();
    }
}
