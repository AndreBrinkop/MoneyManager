package model.asset;

import java.math.BigDecimal;
import java.util.Date;

public class DepotPositionBalance {

    private BigDecimal amount;
    private BigDecimal pricePerUnit;
    private BigDecimal buyValue;
    private Date timestamp;

    public DepotPositionBalance(BigDecimal amount, BigDecimal pricePerUnit, BigDecimal buyValue) {
        this.amount = amount;
        this.pricePerUnit = pricePerUnit;
        this.buyValue = buyValue;
        this.timestamp = new Date();
    }

    public DepotPositionBalance(BigDecimal amount, BigDecimal pricePerUnit) {
        this.amount = amount;
        this.pricePerUnit = pricePerUnit;
        this.buyValue = null;
        this.timestamp = new Date();
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getPricePerUnit() {
        return pricePerUnit;
    }

    public BigDecimal getBuyValue() {
        return buyValue;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
