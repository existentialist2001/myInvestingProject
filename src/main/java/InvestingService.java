import org.jetbrains.annotations.NotNull;
import utility.UtilityService;
import utility.apiRequest.KrStockInfo;
import utility.apiRequest.UsStockInfo;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.time.format.DateTimeFormatter;
public class InvestingService {

    private final Scanner kb = new Scanner(System.in);
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public void start() {

        System.out.println("주식투자 관리 프로그램 시작");

        //미리 데이터 가져오기
        ExcelService.updateTotalPrice();

        while(true) {

            System.out.println("메뉴를 선택하세요:(q입력 시 종료)");
            System.out.println("1: 처음 시작하기");
            System.out.println("2: 신규 주식 입력하기");
            System.out.println("3: 기존 주식 정보 가져오기");
            System.out.println("4: 기존 주식 갱신하기");
            System.out.println("5: 고점 대비 하락률 주가 계산하기");
            System.out.println("6: 현재 주가의 고점대비 하락률 계산하기");
            
            char input = kb.nextLine().charAt(0);

            if (Character.toLowerCase(input) == 'q') {

                System.out.println("프로그램을 종료합니다.");
                break;
            }
            else if (input == '1') {

                getStock(1);
                ExcelService.initExcelAndSave();
            }
            else if (input == '2') {

                getStock(2);
                ExcelService.saveNewStockToExcel();
            }
            else if (input == '3') {
                readStock();
            }

            else if (input == '4') {
                updateStock();
            }
            
            else if (input == '5') {
                getFallPrice();
            }

            else if (input == '6') {
                getFallPercent();
            }

            else {
                System.out.println("잘못된 입력입니다.");
            }
        }
    }

    private void getFallPercent() {

        UtilityService us = new UtilityService();

        String name = getName(1);
        double highPrice = getPrice("고점 가격을 입력하세요:");
        double currentPrice = getPrice("현재 가격을 입력하세요:");
        double percent = us.calculateHowMuchFall(highPrice,currentPrice);

        System.out.println(name + "는 현재 고점대비 " + percent + "% 하락 했습니다.");
    }

    private void getFallPrice() {

        UtilityService us = new UtilityService();

        String name = getName(1);
        double highPrice = getPrice("고점 가격을 입력하세요:");
        int percent = getPercent();

        double targetPrice = us.calculateFallPrice(percent,highPrice);

        System.out.println(name + "의 고점대비 " + percent + "% 하락한 주가는 " + targetPrice + "입니다.");
    }

    private int getPercent() {

        while (true) {

            System.out.print("원하는 하락률을 입력하세요:");

            try {

                int percent = kb.nextInt();
                kb.nextLine();
                return percent;

            } catch (InputMismatchException e) {

                System.out.println("유효한 입력이 아닙니다. 다시 입력해주세요.");
                kb.nextLine();
            }
        }
    }

    public void getStock(int code) {

        //입력받기
        System.out.println("주식 정보를 입력하세요");

        Nation nation = getNation();
        String name = getName(code);

        String ticker = getTicker();

        double buyPrice = getPrice("매수 가격을 입력하세요:");
        double buyHoldings = getHoldings();
        LocalDate firstBuyDate = getDate("최초 매수 날짜를 입력하세요(예:2025-01-01):");
        LocalDate lastBuyDate = getDate("마지막 매수 날짜를 입력하세요(예:2025-01-01):");

        //생성자를 호출해서 생성, 비중까지 업데이트, 총액도 업데이트
        Stock stock = new Stock(nation, name, buyPrice, buyHoldings, firstBuyDate, lastBuyDate, ticker);
        ExcelService.setTotalPrice(stock.calNewStockWeight());

        //portfolio에 저장
        Portfolio.addStock(stock);
        }

    private String getTicker() {
        System.out.print("티커를 입력하세요:");
        return kb.nextLine();
    }

    private String getName(int code) {

        while (true) {

            System.out.print("종목명을 입력하세요:");
            String name = kb.nextLine();

            //if (name.equals("q")) break;

            //비정상흐름(중복발생)
            if (code == 2 && ExcelService.checkSameName(name) == -1) {
                System.out.println("이미 존재하는 주식입니다. 다시 입력해주세요.");
            //정상흐름
            } else {
                return name;
            }
    }
}

    @NotNull
    private LocalDate getDate(String message) {

        while (true) {

            System.out.print(message);
            String buyDateStr = kb.nextLine();

            try {

                LocalDate buyDate = LocalDate.parse(buyDateStr,formatter);
                /*
                옛날에 산 주식일 수 있으니 과거 가능, 단 미래는 불가능
                */
                if (buyDate.isAfter(LocalDate.now())) {

                    System.out.println("미래의 날짜는 입력할 수 없습니다. 다시 입력해주세요.");
                    continue;
                }

                return buyDate;
            }
            catch (DateTimeParseException e) {
                System.out.println("유효한 입력이 아닙니다. 다시 입력해주세요.");
            }
        }
    }

    private double getHoldings() {

        while (true) {

            System.out.print("매수 주식 수를 입력하세요:");

            try {

                double holdings = kb.nextDouble();
                kb.nextLine();
                return holdings;
            }
            catch (InputMismatchException e) {

                System.out.println("유효한 입력이 아닙니다. 다시 입력해주세요.");
                kb.nextLine();
            }
        }
    }

    private double getPrice(String message) {

        while (true) {

            System.out.print(message);

            try {

                double price = kb.nextDouble();
                kb.nextLine();
                return price;
            }
            catch (InputMismatchException e) {

                System.out.println("유효한 입력이 아닙니다. 다시 입력해주세요.");
                kb.nextLine();
            }
        }
    }

    private Nation getNation() {

        while (true) {

            System.out.println("상품이 상장된 국가를 입력하세요:(한국/일본/미국)");
            String nation = kb.nextLine();
            try {
                Nation n = Nation.valueOf(nation);
            }
            catch (IllegalArgumentException e) {

                System.out.println("잘못된 입력입니다. 한국, 일본, 미국 중에서 입력하세요.");
                continue;
            }

            return Nation.valueOf(nation);
        }
    }

    public void readStock() {

        ExcelService.readStocksFromExcel();

        for (Stock stock : Portfolio.getPortfolio()) {

            //티커를 바탕으로 검색해서 현재가, 최고가를 가져오고, 그 가져온 걸 다시 함수에 넣어줘서 하락률 계산
            //한국인지 미국인지에 따라 나눠야지
            double currentStockPrice = 0;
            double highestStockPrice = 0;
            double fallPercent = 0;

            if (stock.getNation().getCurrency().equals("원")) {

                currentStockPrice = KrStockInfo.getCurrentStockPrice(stock.getTicker());
                highestStockPrice = KrStockInfo.getHighestStockPrice(stock.getTicker());
            }
            else {

                currentStockPrice = UsStockInfo.getCurrentStockPrice(stock.getTicker());
                highestStockPrice = UsStockInfo.getHighestStockPrice(stock.getTicker());
            }

            fallPercent = UtilityService.calculateHowMuchFall(highestStockPrice,currentStockPrice);

            //출력 전에 환 단위 정하고 들어가야지
            System.out.println("----------------------------------------");
            System.out.print(stock.getNation().name() + "증시에서 투자중인 " + stock.getName() + "의 평균단가는 " + stock.getAveragePrice() + stock.getNation().getCurrency() + "이며 보유 주식 수는 " + stock.getHoldings() + "주 입니다. ");
            System.out.println("현재까지 투자기간은 총 " + stock.howLongInvesting().getYears() + "년 " + stock.howLongInvesting().getMonths() + "개월 " + stock.howLongInvesting().getDays() + "일 입니다.");
            System.out.println("현재까지 총 매수 금액은 " + stock.getTotalPrice() + stock.getNation().getCurrency() + "입니다.");
            System.out.println("마지막 매수로부터 " + stock.howLongFromLastBuy().getYears() + "년 " + stock.howLongFromLastBuy().getMonths() + "개월 " + stock.howLongFromLastBuy().getDays() + "일 지났습니다.");
            System.out.println("전체에서 차지하는 비중은 " + stock.getWeight() + "%입니다.");
            System.out.println("52주 최고가 대비 " + fallPercent + "% 떨어졌습니다.");

        }
        Portfolio.getPortfolio().clear();
    }

    public void updateStock() {

        while (true) {

            System.out.print("업데이트할 주식 명을 입력하세요:(이전으로 q)");
            String name = kb.nextLine();

            if (name.equals("q")) break;

            int rowNum = ExcelService.findStockFromExcel(name);

            if (rowNum == -1) {
                System.out.println("잘못된 입력이거나 주식을 찾을 수 없습니다.");
            } else {

                //입력
                double buyPrice = getPrice("매수 가격을 입력하세요:");
                double buyHoldings = getHoldings();
                //마지막 매수일도 받기
                LocalDate lastBuyDate = getDate("마지막 매수 날짜를 입력하세요(예:2025-01-01):");

                //Stock 객체 갱신하고 portfolio에 저장
                Stock stock = Portfolio.getPortfolio().get(0);
                stock.additionalBuy(buyPrice,buyHoldings,lastBuyDate);


                //저장함수 호출
                ExcelService.updatePreviousStock(rowNum);
                break;
            }
        }
    }
}