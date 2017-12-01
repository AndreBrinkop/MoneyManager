package model.asset;

import java.math.BigDecimal;

import static util.NumberHelper.roundValue;

public class DepotPosition {

    private String name;
    private String isin;
    private String wkn;

    private Double amount;
    private Double pricePerUnit;
    private Double buyValue;

    public DepotPosition(String name, String isin, String wkn, BigDecimal amount, BigDecimal pricePerUnit, BigDecimal buyValue) {
        this.name = name;
        this.isin = isin;
        this.wkn = wkn;
        this.amount = amount.doubleValue();
        this.pricePerUnit = pricePerUnit.doubleValue();
        this.buyValue = buyValue.doubleValue();
    }

    public DepotPosition(String name, Double amount, Double pricePerUnit) {
        this.name = name;
        this.amount = amount;
        this.pricePerUnit = pricePerUnit;
    }

    public Double getEuroValue() {
        return amount * pricePerUnit;
    }

    public Double getBuyValue() {
        return buyValue;
    }

    public Double getWinLossPercentage() {
        if (buyValue == null) {
            return null;
        }
        return ((pricePerUnit / buyValue) - 1) * 100.0d;
    }

    public Double getTotalWinLoss() {
        if (buyValue == null) {
            return null;
        }
        return (pricePerUnit - buyValue) * amount;
    }

    @Override
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer().append(amount + " " + name + ": " + roundValue(getEuroValue()) + " €");
        if (buyValue != null) {
            Double winLossPercentage = roundValue(getWinLossPercentage());
            stringBuffer.append(" (")
                    .append(winLossPercentage > 0.0d ? "+" : "")
                    .append(winLossPercentage + " %, ")
                    .append(winLossPercentage > 0.0d ? "+" : "")
                    .append(roundValue(getTotalWinLoss()) + " €)");
        }

        return stringBuffer.toString();
    }
}
