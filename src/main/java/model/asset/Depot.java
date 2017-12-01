package model.asset;

import util.NumberHelper;

import java.util.List;

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

    @Override
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(name).append(": ").append(getTotalEurValue()).append(" €").append("\n");

        if (this.depotPositions.isEmpty()) {
            return stringBuffer.toString();
        }

        this.depotPositions.stream().forEach(position -> stringBuffer.append("\t\t").append(position.toString()));

        return stringBuffer.substring(0, stringBuffer.length() - 1);
    }

}
