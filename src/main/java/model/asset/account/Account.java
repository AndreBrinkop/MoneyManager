package model.asset.account;

import model.asset.Balance;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public abstract class Account {

    String name;
    protected List<Balance> balances = new LinkedList<>();

    public Account(String name) {
        this.name = name;
    }

    public Balance getCurrentBalance() {
        return this.balances.stream().max(Comparator.comparing(Balance::getTimestamp)).orElse(null);
    }

    public List<Balance> getHistoricTotalEurValues() {
        return this.balances;
    }

    public void updateBalance(Account account) {
        if (account == null) {
            this.balances.add(new Balance(BigDecimal.ZERO));
        } else {
            this.balances.add(account.getCurrentBalance());
        }
    }

    public String getName() {
        return name;
    }
}
