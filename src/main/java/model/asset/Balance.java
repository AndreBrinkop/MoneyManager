package model.asset;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.Date;

@Entity
public class Balance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long balanceId;

    private Date timestamp;
    private String balanceString;
    private String currency = "EUR";
    private String exchangeRateToEurString = "1.0";

    public Balance() {
    }

    public Balance(Date timestamp, BigDecimal balance) {
        this.timestamp = timestamp;
        this.balanceString = balance.toString();
    }

    public Balance(BigDecimal balance) {
        this.timestamp = new Date();
        this.balanceString = balance.toString();
    }

    public Balance(Date timestamp, BigDecimal balance, String currency, BigDecimal exchangeRateToEur) {
        this.timestamp = timestamp;
        this.balanceString = balance.toString();
        this.currency = currency;
        this.exchangeRateToEurString = exchangeRateToEur.toString();
    }

    public Balance(BigDecimal balance, String currency, BigDecimal exchangeRateToEur) {
        this.timestamp = new Date();
        this.balanceString = balance.toString();
        this.currency = currency;
        this.exchangeRateToEurString = exchangeRateToEur.toString();
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public BigDecimal getBalanceValue() {
        return new BigDecimal(balanceString);
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getExchangeRateToEur() {
        return new BigDecimal(exchangeRateToEurString);
    }

    public BigDecimal getEuroBalanceValue() {
        return new BigDecimal(balanceString).multiply(new BigDecimal(exchangeRateToEurString));
    }
}
