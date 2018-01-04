package model.asset.account;

import model.asset.Balance;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.math.BigDecimal;

import static util.NumberHelper.roundValue;

@Entity
@DiscriminatorValue("CurrencyAccount")
public class CurrencyAccount extends Account {

    private String currency;

    public CurrencyAccount() {
    }

    public CurrencyAccount(String name, String currency, BigDecimal currencyValue, BigDecimal exchangeRateToEur) {
        super(name);
        this.currency = currency;
        this.balances.add(new Balance(currencyValue, currency, exchangeRateToEur));
    }

    public String getCurrency() {
        return currency;
    }

    @Override
    public String toString() {
        Balance balance = getCurrentEurBalance();
        return name + ": " + getCurrentEurBalance().getBalanceValue().stripTrailingZeros() + " " + currency + ": " + roundValue(getCurrentEurBalance().getEuroBalanceValue()) + " €";
    }

}
