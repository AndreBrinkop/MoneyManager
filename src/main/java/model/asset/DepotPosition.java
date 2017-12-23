package model.asset;

import java.math.BigDecimal;
import java.math.MathContext;

import static util.NumberHelper.roundValue;

public class DepotPosition {

    private String name;
    private String isin;
    private String wkn;

    private BigDecimal amount;
    private BigDecimal pricePerUnit;
    private BigDecimal buyValue;

    public DepotPosition(String name, String isin, String wkn, BigDecimal amount, BigDecimal pricePerUnit, BigDecimal buyValue) {
        this.name = name;
        this.isin = isin;
        this.wkn = wkn;
        this.amount = amount;
        this.pricePerUnit = pricePerUnit;
        this.buyValue = buyValue;
    }

    public DepotPosition(String name, BigDecimal amount, BigDecimal pricePerUnit) {
        this.name = name;
        this.amount = amount;
        this.pricePerUnit = pricePerUnit;
    }

    public BigDecimal getEuroValue() {
        return amount.multiply(pricePerUnit);
    }

    public BigDecimal getBuyValue() {
        return buyValue;
    }

    public BigDecimal getWinLossPercentage() {
        if (buyValue == null) {
            return null;
        }
        return (pricePerUnit.divide(buyValue, MathContext.DECIMAL128).subtract(new BigDecimal(1))).multiply(new BigDecimal(100));
    }

    public BigDecimal getTotalWinLoss() {
        if (buyValue == null) {
            return null;
        }
        return (pricePerUnit.subtract(buyValue)).multiply(amount);
    }

    @Override
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer().append(amount + " " + name + ": " + roundValue(getEuroValue()) + " €");
        if (buyValue != null) {
            BigDecimal winLossPercentage = roundValue(getWinLossPercentage());
            stringBuffer.append(" (")
                    .append(winLossPercentage.doubleValue() > 0.0d ? "+" : "")
                    .append(winLossPercentage + " %, ")
                    .append(winLossPercentage.doubleValue() > 0.0d ? "+" : "")
                    .append(roundValue(getTotalWinLoss()) + " €)");
        }

        return stringBuffer.toString();
    }
}
