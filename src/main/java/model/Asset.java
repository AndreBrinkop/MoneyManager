package model;

public class Asset {

    private String description;
    private Double currencyValue;
    private String currency;
    private Double euroValue;
    private boolean showAsset = true;

    public Asset(Double currencyValue, String currency, Double euroValue) {
        this.currencyValue = currencyValue;
        this.currency = currency;
        this.euroValue = euroValue;
    }

    public Asset(Double euroValue) {
        this.currencyValue = euroValue;
        this.currency = "€";
        this.euroValue = euroValue;
    }

    public Asset(Double euroValue, String description) {
        this(euroValue);
        this.description = description;
    }

    public Double getCurrencyValue() {
        return currencyValue;
    }

    public String getCurrency() {
        return currency;
    }

    Double getEuroValue() {
        return euroValue;
    }

    public String getDescription() {
        return description;
    }

    public boolean isShowAsset() {
        return showAsset;
    }

    public void setShowAsset(boolean showAsset) {
        this.showAsset = showAsset;
    }

    @Override
    public String toString() {
        if ("€".equals(currency) || "EUR".equals(currency)) {
            if (this.description != null && !this.description.isEmpty()) {
                return description + ": " + euroValue + " " + currency;
            }
            return euroValue + " " + currency;
        }
        return currencyValue + " " + currency + " = " + roundValue(euroValue) + " €";
    }

    static Double roundValue(Double averagePrice) {
        int scale = (int) Math.pow(10, 2);
        return (double) Math.round(averagePrice * scale) / scale;
    }

}
