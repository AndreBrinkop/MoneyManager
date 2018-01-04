package model.asset;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.Date;

@Entity
public class DepotPositionBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long depotPositionBalanceId;

    private String amountString;
    private String pricePerUnitString;
    private String buyValueString;
    private Date timestamp;

    public DepotPositionBalance() {
    }

    public DepotPositionBalance(BigDecimal amount, BigDecimal pricePerUnit, BigDecimal buyValue) {
        this.amountString = amount.toString();
        this.pricePerUnitString = pricePerUnit.toString();
        this.buyValueString = buyValue.toString();
        this.timestamp = new Date();
    }

    public DepotPositionBalance(BigDecimal amount, BigDecimal pricePerUnit) {
        this.amountString = amount.toString();
        this.pricePerUnitString = pricePerUnit.toString();
        this.buyValueString = null;
        this.timestamp = new Date();
    }

    public BigDecimal getAmount() {
        return amountString == null ? null : new BigDecimal(amountString);

    }

    public BigDecimal getPricePerUnit() {
        return pricePerUnitString == null ? null : new BigDecimal(pricePerUnitString);
    }

    public BigDecimal getBuyValue() {
        return buyValueString == null ? null : new BigDecimal(buyValueString);
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
