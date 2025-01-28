import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;

public class Stock {

    //국가
    private Nation nation;

    //변수
    private String name;

    //avgPrice와 holdings 둘다 소수점 둘째자리까지
    //평균 단가
    private BigDecimal averagePrice;

    //주식 수, 요즘에는 소수점도 있으니
    private BigDecimal holdings;

    private BigDecimal totalPrice;

    //최초 매수일, 투자 기간을 계산할 때 사용
    private LocalDate firstBuyDate;

    //메서드
    //여기 함수들 구조를 어떻게 할지도 정해야함, return 해줘서 InvestingService에서 그 리턴 값을 받아서 쓸지, 아니면 바로 결과 출력할 지, 구조도 그려보고 구체화 하기 -> 일단은 여기서 다 출력하는 걸로

    public Stock(Nation nation, String name, double price, double holdings, LocalDate firstBuyDate) {

        this.nation = nation;
        this.name = name;
        this.averagePrice = BigDecimal.valueOf(price);
        this.holdings = BigDecimal.valueOf(holdings);
        this.firstBuyDate = firstBuyDate;

        //생성자에서 총액 구하기
        this.totalPrice = averagePrice.multiply(this.holdings);
    }

    //투자기간 계산기
    public Period howLongInvesting() {

        LocalDate now = LocalDate.now();
        Period period = Period.between(firstBuyDate,now);
        return period;
    }

    //추가 매수 했을 때
    public void additionalBuy(double price, double holdings) {

        //분자
        BigDecimal previousTotalPrice = averagePrice.multiply(this.holdings);

        BigDecimal newHoldings = BigDecimal.valueOf(holdings);
        BigDecimal newPrice = BigDecimal.valueOf(price);
        BigDecimal newTotalPrice = newPrice.multiply(newHoldings);

        BigDecimal totalPrice = previousTotalPrice.add(newTotalPrice);

        //분모
        BigDecimal totalHoldings = this.holdings.add(newHoldings);

        //계산
        BigDecimal finalAvgPrice = totalPrice.divide(totalHoldings,2,RoundingMode.HALF_UP);

        //반영
        this.holdings = totalHoldings;
        averagePrice = finalAvgPrice;

        //추가매수 시 총 매수 금액도 갱신
        totalPrice = this.holdings.multiply(averagePrice);
        
        System.out.println(name + "을(를) " + price + "에 " + holdings + "주만큼 추가 매수하여 현재 보유 주식 수는 " + this.holdings + "주이며 평균단가는 " + averagePrice + "입니다.");
    }

    //수익률 계산기
    /*
    나중에 달러 수익률과 원화 수익률 나누기
    */
    public void calculateReturn(double price) {

        BigDecimal currentPrice = BigDecimal.valueOf(price);
        BigDecimal profitRate = currentPrice.subtract(averagePrice).divide(averagePrice,2,RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        System.out.println(name + "의 현재까지의 수익률은 " + profitRate + "%입니다.");
    }

    //목표 수익률이 되려면 주가가 얼마여야 하는 지 계산기
    public void targetPrice(int targetReturn) {

        BigDecimal targetPrice = averagePrice.add(averagePrice.multiply(BigDecimal.valueOf(targetReturn)).divide(BigDecimal.valueOf(100),2,RoundingMode.HALF_UP)).setScale(2,RoundingMode.HALF_UP);
        System.out.println(name + "로 " + targetReturn + "%의 수익률을 내기 위해서는 주가가 " + targetPrice +"가 되어야 합니다.");
    }
    
    //getter

    public String getName() {
        return name;
    }

    public Nation getNation() { return nation; }

    public double getAveragePrice() {
        return averagePrice.doubleValue();
    }

    public double getHoldings() {
        return holdings.doubleValue();
    }

    public LocalDate getFirstBuyDate() {
        return firstBuyDate;
    }

    public double getTotalPrice() { return totalPrice.doubleValue(); }
}
