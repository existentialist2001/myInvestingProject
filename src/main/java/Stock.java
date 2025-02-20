import utility.apiRequest.Currency;

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

    //마지막 매수일
    private LocalDate lastBuyDate;

    //전체에서 차지하는 비중
    private BigDecimal weight;

    //메서드
    //여기 함수들 구조를 어떻게 할지도 정해야함, return 해줘서 InvestingService에서 그 리턴 값을 받아서 쓸지, 아니면 바로 결과 출력할 지, 구조도 그려보고 구체화 하기 -> 일단은 여기서 다 출력하는 걸로

    public Stock(Nation nation, String name, double price, double holdings, LocalDate firstBuyDate, LocalDate lastBuyDate) {

        this.nation = nation;
        this.name = name;
        this.averagePrice = BigDecimal.valueOf(price);
        this.holdings = BigDecimal.valueOf(holdings);
        this.firstBuyDate = firstBuyDate;
        this.lastBuyDate = lastBuyDate;

        //생성자에서 총액 구하기
        this.totalPrice = averagePrice.multiply(this.holdings);
        
    }

    //신규 주식 입력 시
    /*
    엑셀에서 가져온 총액에는, 신규 주식의 총액이 반영되어 있지 않음,
    그래서 가져온 총액 + 신규 주식의 총액 -> 분모
    신규 주식의 총액 -> 분자
    */
    public double calNewStockWeight() {

        //엑셀에서 총액 계산해서 가져오기, 최신 총액 맞음
        BigDecimal bdTotalPrice = BigDecimal.valueOf(ExcelService.getTotalPrice());
        
        //신규 주식의 총액
        BigDecimal temp = this.totalPrice;
        BigDecimal newStockTotalPrice;

        //신규 주식의 총액을 원화로 변환해주는 과정
        if (nation.getCurrency().equals("달러")) {

            BigDecimal us = BigDecimal.valueOf(Currency.getWonDollarExRate());
            newStockTotalPrice = temp.multiply(us);
        }
        else if (nation.getCurrency().equals("엔")) {

            BigDecimal jp = BigDecimal.valueOf(Currency.getWonYenExRate());
            newStockTotalPrice = temp.multiply(jp);
        }
        else {
            newStockTotalPrice = temp;
        }
        
        //분모
        BigDecimal finalTotalPrice = bdTotalPrice.add(newStockTotalPrice);
        //계산
        this.weight = newStockTotalPrice.divide(finalTotalPrice,2,RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

        return finalTotalPrice.doubleValue();
    }

    //투자기간 계산기 - 최초 매수일 기준
    public Period howLongInvesting() {
        return Period.between(firstBuyDate,LocalDate.now());
    }

    //투자기간 계산기 - 마지막 매수일 기준
    public Period howLongFromLastBuy() {
        return Period.between(lastBuyDate,LocalDate.now());
    }

    //추가 매수 했을 때
    public void additionalBuy(double price, double holdings, LocalDate lastBuyDate) {

        //
        //기존 총액
        BigDecimal previousTotalPrice = BigDecimal.valueOf(ExcelService.getTotalPrice());


        //신규 개별주식 총액
        BigDecimal newHoldings = BigDecimal.valueOf(holdings);
        BigDecimal newPrice = BigDecimal.valueOf(price);
        BigDecimal newTotalPrice = newPrice.multiply(newHoldings).add(previousTotalPrice);

        //기존 총액 갱신
        BigDecimal finalTotalPrice = previousTotalPrice.add(newTotalPrice);
        ExcelService.setTotalPrice(finalTotalPrice.doubleValue());

        //기존 개별주식 총액 갱신
        this.totalPrice = this.totalPrice.add(newTotalPrice);

        //비중 갱신
        this.weight = this.totalPrice.divide(finalTotalPrice,0,RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));


        BigDecimal totalHoldings = this.holdings.add(newHoldings);

        //계산
        BigDecimal finalAvgPrice = this.totalPrice.divide(totalHoldings,2,RoundingMode.HALF_UP);

        //반영
        this.holdings = totalHoldings;
        this.averagePrice = finalAvgPrice;


        //추가매수 시 마지막 매수 날짜도 갱신, 입력을 받아야함
        this.lastBuyDate = lastBuyDate;

        System.out.println(name + "을(를) " + price + "에 " + holdings + "주만큼 추가 매수하여 현재 보유 주식 수는 " + this.holdings + "주이며 평균단가는 " + averagePrice + "입니다.");
        System.out.println("전체에서 차지하는 비중은 " + weight.intValue() + "%입니다.");
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

    public double getTotalPrice() { return totalPrice.doubleValue();}

    public LocalDate getLastBuyDate() { return lastBuyDate; }

    public int getWeight() { return weight.intValue(); }
}
