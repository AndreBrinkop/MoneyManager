package util;

public class NumberHelper {

    public static Double roundValue(Double averagePrice) {
        int scale = (int) Math.pow(10, 2);
        return (double) Math.round(averagePrice * scale) / scale;
    }

}
