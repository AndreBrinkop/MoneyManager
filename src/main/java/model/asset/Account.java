package model.asset;

import java.math.BigDecimal;

public abstract class Account {

    String name;

    public Account(String name) {
        this.name = name;
    }

    public abstract BigDecimal getTotalEurValue();

    public String getName() {
        return name;
    }
}
