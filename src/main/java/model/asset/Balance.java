package model.asset;

import java.math.BigDecimal;
import java.util.Date;

public class Balance {

    private Date timestamp;
    private BigDecimal balance;
    private String currency = "EUR";
    private BigDecimal exchangeRateToEur = BigDecimal.ONE;

    public Balance(Date timestamp, BigDecimal balance) {
        this.timestamp = timestamp;
        this.balance = balance;
    }

    public Balance(BigDecimal balance) {
        this.timestamp = new Date();
        this.balance = balance;
    }

    public Balance(Date timestamp, BigDecimal balance, String currency, BigDecimal exchangeRateToEur) {
        this.timestamp = timestamp;
        this.balance = balance;
        this.currency = currency;
        this.exchangeRateToEur = exchangeRateToEur;
    }

    public Balance(BigDecimal balance, String currency, BigDecimal exchangeRateToEur) {
        this.timestamp = new Date();
        this.balance = balance;
        this.currency = currency;
        this.exchangeRateToEur = exchangeRateToEur;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public BigDecimal getBalanceValue() {
        return balance;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getExchangeRateToEur() {
        return exchangeRateToEur;
    }

    public BigDecimal getEuroBalanceValue() {
        return balance.multiply(exchangeRateToEur);
    }
}
