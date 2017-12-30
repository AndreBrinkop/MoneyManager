package model.asset.account;

import model.asset.Balance;

import java.math.BigDecimal;

import static util.NumberHelper.roundValue;

public class CurrencyAccount extends Account {

    private String currency;

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
        Balance balance = getCurrentBalance();
        return name + ": " + getCurrentBalance().getBalanceValue().stripTrailingZeros() + " " + currency + ": " + roundValue(getCurrentBalance().getEuroBalanceValue()) + " €";
    }

}
