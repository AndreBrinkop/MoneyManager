package model.asset;

public abstract class Account {

    String name;

    public Account(String name) {
        this.name = name;
    }

    public abstract Double getTotalEurValue();

    public String getName() {
        return name;
    }
}
