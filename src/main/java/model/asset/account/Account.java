package model.asset.account;

import model.asset.AssetObject;
import model.asset.Balance;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
        name = "account_type",
        discriminatorType = DiscriminatorType.STRING
)
public abstract class Account implements AssetObject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long accountId;

    String name;

    @OneToMany(cascade = {CascadeType.ALL})
    protected List<Balance> balances = new LinkedList<>();

    protected Account() {
    }

    public Account(String name) {
        this.name = name;
    }

    public Balance getCurrentEurBalance() {
        return this.balances.stream().max(Comparator.comparing(Balance::getTimestamp)).orElse(null);
    }

    @Override
    public Balance getCurrentEurBalance(List<String> ignoredAccountNames) {
        if (ignoredAccountNames == null) {
            ignoredAccountNames = new LinkedList<>();
        }
        if (ignoredAccountNames.contains(this.getName())) {
            return new Balance(BigDecimal.ZERO);
        }
        return getCurrentEurBalance();
    }

    public List<Balance> getHistoricTotalEurValues() {
        return this.balances;
    }

    public void updateBalance(Account account) {
        if (account == null) {
            this.balances.add(new Balance(BigDecimal.ZERO));
        } else {
            this.balances.add(account.getCurrentEurBalance());
        }
    }

    public String getName() {
        return name;
    }
}
