package model.asset;

import util.NumberHelper;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

import static util.NumberHelper.roundValue;

public class Depot extends Account {

    private List<DepotPosition> depotPositions;

    public Depot(String name, List<DepotPosition> depotPositions) {
        super(name);
        this.depotPositions = depotPositions;
    }

    @Override
    public BigDecimal getTotalEurValue() {
        return NumberHelper.roundValue(this.depotPositions.stream().map(DepotPosition::getEuroValue).reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    public BigDecimal getTotalWinLoss() {
        if (this.depotPositions.stream().filter(depotPosition -> depotPosition.getBuyValue() == null).findAny().isPresent()) {
            return null;
        }
        return this.depotPositions.stream().map(DepotPosition::getTotalWinLoss).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalWinLossPercentage() {
        if (this.depotPositions.stream().filter(depotPosition -> depotPosition.getBuyValue() == null).findAny().isPresent()) {
            return null;
        }
        BigDecimal totalValue = getTotalEurValue();
        BigDecimal totalBuyValue = totalValue.subtract(getTotalWinLoss());
        return (totalValue.divide(totalBuyValue, MathContext.DECIMAL128).subtract(new BigDecimal(1))).multiply(new BigDecimal(100));
    }

    @Override
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(name + ": " + roundValue(getTotalEurValue()) + " €");
        BigDecimal winLossPercentage = roundValue(getTotalWinLossPercentage());
        if (winLossPercentage != null) {
            stringBuffer.append(" (")
                    .append(winLossPercentage.doubleValue() > 0.0d ? "+" : "")
                    .append(winLossPercentage + " %, ")
                    .append(winLossPercentage.doubleValue() > 0.0d ? "+" : "")
                    .append(roundValue(getTotalWinLoss()) + " €)");
        }
        stringBuffer.append("\n");

        if (this.depotPositions.isEmpty()) {
            return stringBuffer.toString();
        }

        this.depotPositions.stream().forEach(position -> stringBuffer.append("\t\t").append(position).append("\n"));

        return stringBuffer.substring(0, stringBuffer.length() - 1);
    }


}
