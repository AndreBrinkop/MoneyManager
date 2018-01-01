package model.asset.account;

import model.asset.Balance;

import java.math.BigDecimal;

public class BasicAccount extends Account {

    public BasicAccount(String name, BigDecimal euroBalance) {
        super(name);
        this.balances.add(new Balance(euroBalance));
    }

    @Override
    public String toString() {
        return this.name + ": " + getCurrentBalance().getBalanceValue().stripTrailingZeros().toPlainString() + " €";
    }
}

