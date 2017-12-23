package model.asset;

import java.math.BigDecimal;

import static util.NumberHelper.roundValue;

public class CurrencyAccount extends Account {

    private BigDecimal currencyValue;
    private String currency;
    private BigDecimal euroValue;

    public CurrencyAccount(String name, String currency, BigDecimal currencyValue, BigDecimal euroValue) {
        super(name);
        this.currencyValue = currencyValue;
        this.currency = currency;
        this.euroValue = euroValue;
    }

    public BigDecimal getCurrencyValue() {
        return currencyValue;
    }

    public String getCurrency() {
        return currency;
    }

    @Override
    public BigDecimal getTotalEurValue() {
        return euroValue;
    }

    @Override
    public String toString() {
        return name + ": " + currencyValue + " " + currency + ": " + roundValue(euroValue) + " €";
    }


}
