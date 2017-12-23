package model.asset;

import java.math.BigDecimal;

public class BasicAccount extends Account {

    private BigDecimal euroBalance;

    public BasicAccount(String name, BigDecimal euroBalance) {
        super(name);
        this.euroBalance = euroBalance;
    }

    @Override
    public BigDecimal getTotalEurValue() {
        return euroBalance;
    }

    @Override
    public String toString() {
        return this.name + ": " + getTotalEurValue() + " €";
    }
}
