package model.asset.account;

import model.asset.Balance;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.math.BigDecimal;

@Entity
@DiscriminatorValue("BasicAccount")
public class BasicAccount extends Account {

    public BasicAccount() {
    }

    public BasicAccount(String name, BigDecimal euroBalance) {
        super(name);
        this.balances.add(new Balance(euroBalance));
    }

    @Override
    public String toString() {
        return this.name + ": " + getCurrentEurBalance().getBalanceValue().stripTrailingZeros().toPlainString() + " €";
    }
}

