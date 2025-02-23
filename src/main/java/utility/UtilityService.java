package utility;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class UtilityService {

    public static double calculateFallPrice(int percent, double highPrice) {

    BigDecimal bHighPrice = BigDecimal.valueOf(highPrice);
    BigDecimal bPercent = BigDecimal.valueOf(percent);

    BigDecimal minus = bHighPrice.multiply(bPercent).divide(BigDecimal.valueOf(100),2,RoundingMode.HALF_UP);
    BigDecimal targetPrice = bHighPrice.subtract(minus);

    return targetPrice.doubleValue();
    }

    public static double calculateHowMuchFall(double highPrice, double currentPrice) {

        BigDecimal bHighPrice = BigDecimal.valueOf(highPrice);
        BigDecimal bCurrentPrice = BigDecimal.valueOf(currentPrice);

        return bHighPrice.subtract(bCurrentPrice).divide(bHighPrice,4,RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue();
    }

    /*public static void main(String[] args) {

        double percent = calculateHowMuchFall(226.40,198.24);
        System.out.println("percent = " + percent);
    }*/
}