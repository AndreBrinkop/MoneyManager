package model;

import java.util.LinkedList;

public class AssetList extends LinkedList<Asset> {

    private String assetLocation;

    private AssetList() {
        super();
    }

    public AssetList(String assetLocation) {
        this.assetLocation = assetLocation;
    }

    public Double getTotalEurValue() {
        return Asset.roundValue(this.stream().filter(Asset::isShowAsset).mapToDouble(Asset::getEuroValue).sum());
    }

    @Override
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(assetLocation).append(" - ").append(getTotalEurValue()).append(" €").append(this.isEmpty() ? "\n" : ":\n");

        this.stream().filter(Asset::isShowAsset).forEach(asset -> stringBuffer.append("\t").append(asset.toString()).append("\n"));

        return stringBuffer.toString();
    }
}
