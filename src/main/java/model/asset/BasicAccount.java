package model.asset;

public class BasicAccount extends Account {

    private Double euroBalance;

    public BasicAccount(String name, Double euroBalance) {
        super(name);
        this.euroBalance = euroBalance;
    }

    @Override
    public Double getTotalEurValue() {
        return euroBalance;
    }

    @Override
    public String toString() {
        return this.name + ": " + getTotalEurValue() + " €";
    }
}
