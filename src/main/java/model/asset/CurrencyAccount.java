package model.asset;

import static util.NumberHelper.roundValue;

public class CurrencyAccount extends Account {

    private Double currencyValue;
    private String currency;
    private Double euroValue;

    public CurrencyAccount(String name, String currency, Double currencyValue, Double euroValue) {
        super(name);
        this.currencyValue = currencyValue;
        this.currency = currency;
        this.euroValue = euroValue;
    }

    public Double getCurrencyValue() {
        return currencyValue;
    }

    public String getCurrency() {
        return currency;
    }

    @Override
    public Double getTotalEurValue() {
        return euroValue;
    }

    @Override
    public String toString() {
        return name + ": " + currencyValue + " " + currency + ": " + roundValue(euroValue) + " €";
    }


}
