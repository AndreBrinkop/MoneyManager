package model.asset.account;

import model.asset.Balance;
import util.NumberHelper;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.stream.Collectors;

import static util.NumberHelper.roundValue;

public class Depot extends Account {

    private List<DepotPosition> depotPositions;

    public Depot(String name, List<DepotPosition> depotPositions) {
        super(name);
        this.depotPositions = depotPositions;
        this.balances.add(new Balance(getCurrentTotalEuroValue()));
    }

    public void updateBalance(Account account) {
        if (!(account instanceof Depot)) {
            return;
        }
        Depot depot = (Depot) account;
        super.updateBalance(account);

        List<DepotPosition> currentDepotPositions = depot.getDepotPositions();
        for (DepotPosition currentDepotPosition : currentDepotPositions) {
            int positionIndex = depotPositions.stream().map(depotPosition -> depotPosition.getName()).collect(Collectors.toList()).indexOf(currentDepotPosition.getName());
            if (positionIndex > -1) {
                // position already existed
                depotPositions.get(positionIndex).updatePosition(currentDepotPosition);
            } else {
                // add new position
                depotPositions.add(currentDepotPosition);
            }
        }

        for (DepotPosition depotPosition : depotPositions) {
            // position was removed
            if (!currentDepotPositions.stream().map(currentPosition -> currentPosition.getName()).collect(Collectors.toList()).contains(depotPosition.getName())) {
                depotPosition.updatePosition(null);
            }
        }
    }

    private BigDecimal getCurrentTotalEuroValue() {
        return NumberHelper.roundValue(this.depotPositions.stream().map(DepotPosition::getCurrentEuroValue).reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    public BigDecimal getTotalWinLoss() {
        if (this.depotPositions.stream().filter(depotPosition -> depotPosition.getCurrentBuyValue() == null).findAny().isPresent()) {
            return null;
        }
        return this.depotPositions.stream().map(DepotPosition::getTotalWinLoss).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalWinLossPercentage() {
        if (this.depotPositions.stream().filter(depotPosition -> depotPosition.getCurrentBuyValue() == null).findAny().isPresent()) {
            return null;
        }
        BigDecimal totalValue = getCurrentBalance().getBalanceValue();
        BigDecimal totalBuyValue = totalValue.subtract(getTotalWinLoss());
        return (totalValue.divide(totalBuyValue, MathContext.DECIMAL128).subtract(new BigDecimal(1))).multiply(new BigDecimal(100));
    }

    public List<DepotPosition> getDepotPositions() {
        return depotPositions;
    }

    @Override
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(name + ": " + roundValue(getCurrentBalance().getBalanceValue()) + " €");
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
