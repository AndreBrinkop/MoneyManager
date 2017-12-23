package util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class NumberHelper {

    public static BigDecimal roundValue(BigDecimal value) {
        if (value == null) {
            return value;
        }
        return value.setScale(2, RoundingMode.CEILING);
    }

}
