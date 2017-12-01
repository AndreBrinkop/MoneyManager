package model.asset;

import java.math.BigDecimal;

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
        this.buyValue = buyValue;
    }

    public Double getEuroValue() {
        return amount * pricePerUnit;
    }

    @Override
    public String toString() {
        return amount + " " + name + ": " + getEuroValue() + " €";
    }
}
