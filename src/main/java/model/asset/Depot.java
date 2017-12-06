package model.asset;

import util.NumberHelper;

import java.util.List;

import static util.NumberHelper.roundValue;

public class Depot extends Account {

    private List<DepotPosition> depotPositions;

    public Depot(String name, List<DepotPosition> depotPositions) {
        super(name);
        this.depotPositions = depotPositions;
    }

    @Override
    public Double getTotalEurValue() {
        return NumberHelper.roundValue(this.depotPositions.stream().mapToDouble(DepotPosition::getEuroValue).sum());
    }

    public Double getTotalWinLoss() {
        if (this.depotPositions.stream().filter(depotPosition -> depotPosition.getBuyValue() == null).findAny().isPresent()) {
            return null;
        }
        return this.depotPositions.stream().mapToDouble(DepotPosition::getTotalWinLoss).sum();
    }

    public Double getTotalWinLossPercentage() {
        if (this.depotPositions.stream().filter(depotPosition -> depotPosition.getBuyValue() == null).findAny().isPresent()) {
            return null;
        }
        Double totalValue = getTotalEurValue();
        Double totalBuyValue = totalValue - getTotalWinLoss();
        return (totalValue / totalBuyValue - 1) * 100;
    }

    @Override
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(name + ": " + roundValue(getTotalEurValue()) + " €");
        Double winLossPercentage = roundValue(getTotalWinLossPercentage());
        if (winLossPercentage != null) {
            stringBuffer.append(" (")
                    .append(winLossPercentage > 0.0d ? "+" : "")
                    .append(winLossPercentage + " %, ")
                    .append(winLossPercentage > 0.0d ? "+" : "")
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
