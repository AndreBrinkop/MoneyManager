package model.asset;

public class DepotPosition {

    private String name;
    private Double amount;
    private Double euroValue;

    public DepotPosition(String name, Double amount, Double euroValue) {
        this.name = name;
        this.amount = amount;
        this.euroValue = euroValue;
    }

    public Double getEuroValue() {
        return euroValue;
    }

    @Override
    public String toString() {
        return amount + " " + name + ": " + euroValue + " €";
    }
}
