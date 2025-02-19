package utility;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class UtilityService {

    public double calculateFallPrice(int percent, double highPrice) {

    BigDecimal bHighPrice = BigDecimal.valueOf(highPrice);
    BigDecimal bPercent = BigDecimal.valueOf(percent);

    BigDecimal minus = bHighPrice.multiply(bPercent).divide(BigDecimal.valueOf(100),2,RoundingMode.HALF_UP);
    BigDecimal targetPrice = bHighPrice.subtract(minus);

    return targetPrice.doubleValue();
    }

    public int calculateHowMuchFall(double highPrice, double currentPrice) {

        BigDecimal bHighPrice = BigDecimal.valueOf(highPrice);
        BigDecimal bCurrentPrice = BigDecimal.valueOf(currentPrice);

        return bHighPrice.subtract(bCurrentPrice).divide(bHighPrice,2,RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).intValue();
    }
}
