package model.asset.account;

import model.asset.DepotPositionBalance;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import static util.NumberHelper.roundValue;

public class DepotPosition {

    private String name;
    private String isin;
    private String wkn;
    private List<DepotPositionBalance> positionBalanceList = new LinkedList<>();

    public DepotPosition(String name, String isin, String wkn, BigDecimal amount, BigDecimal pricePerUnit, BigDecimal buyValue) {
        this.name = name;
        this.isin = isin;
        this.wkn = wkn;
        this.positionBalanceList.add(new DepotPositionBalance(amount, pricePerUnit, buyValue));
    }

    public DepotPosition(String name, BigDecimal amount, BigDecimal pricePerUnit) {
        this.name = name;
        this.positionBalanceList.add(new DepotPositionBalance(amount, pricePerUnit));

    }

    public DepotPositionBalance getCurrentDepotPositionBalance() {
        return this.positionBalanceList.stream().max(Comparator.comparing(DepotPositionBalance::getTimestamp)).orElse(null);
    }

    public BigDecimal getCurrentEuroValue() {
        return getCurrentDepotPositionBalance().getAmount().multiply(getCurrentDepotPositionBalance().getPricePerUnit());
    }

    public BigDecimal getCurrentBuyValue() {
        return getCurrentDepotPositionBalance().getBuyValue();
    }

    public BigDecimal getWinLossPercentage() {
        if (getCurrentDepotPositionBalance().getBuyValue() == null) {
            return null;
        }
        return (getCurrentDepotPositionBalance().getPricePerUnit().divide(getCurrentDepotPositionBalance().getBuyValue(), MathContext.DECIMAL128).subtract(new BigDecimal(1))).multiply(new BigDecimal(100));
    }

    public BigDecimal getTotalWinLoss() {
        if (getCurrentDepotPositionBalance().getBuyValue() == null) {
            return null;
        }
        return (getCurrentDepotPositionBalance().getPricePerUnit().subtract(getCurrentDepotPositionBalance().getBuyValue())).multiply(getCurrentDepotPositionBalance().getAmount());
    }

    public String getName() {
        return name;
    }

    public String getIsin() {
        return isin;
    }

    public String getWkn() {
        return wkn;
    }

    @Override
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer().append(getCurrentDepotPositionBalance().getAmount() + " " + name + ": " + roundValue(getCurrentEuroValue()) + " €");
        if (getCurrentDepotPositionBalance().getBuyValue() != null) {
            BigDecimal winLossPercentage = roundValue(getWinLossPercentage());
            stringBuffer.append(" (")
                    .append(winLossPercentage.doubleValue() > 0.0d ? "+" : "")
                    .append(winLossPercentage + " %, ")
                    .append(winLossPercentage.doubleValue() > 0.0d ? "+" : "")
                    .append(roundValue(getTotalWinLoss()) + " €)");
        }

        return stringBuffer.toString();
    }

    public void updatePosition(DepotPosition currentDepotPosition) {
        if (currentDepotPosition == null) {
            this.positionBalanceList.add(new DepotPositionBalance(BigDecimal.ZERO, BigDecimal.ZERO));
        } else {
            this.positionBalanceList.add(currentDepotPosition.getCurrentDepotPositionBalance());
        }
    }
}
