import org.jetbrains.annotations.NotNull;

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

        while(true) {

            System.out.println("메뉴를 선택하세요:(q입력 시 종료)");
            System.out.println("1: 처음 시작하기");
            System.out.println("2: 신규 주식 입력하기");
            System.out.println("3: 기존 주식 정보 가져오기");
            System.out.println("4: 기존 주식 갱신하기");

            char input = kb.nextLine().charAt(0);

            if (input == 'q') {

                System.out.println("프로그램을 종료합니다.");
                break;
            }
            else if (input == '1') {

                getStock();
                ExcelService.initExcelAndSave();
            }
            else if (input == '2') {

                getStock();
                ExcelService.saveNewStockToExcel();
            }
            else if (input == '3') {
                readStock();
            }

            else if (input == '4') {
                updateStock();
            }
            else {
                System.out.println("잘못된 입력입니다.");
            }
        }
    }

    public void getStock() {

        /*
        종목명 - 신규 주식 입력인지, 기존 주식 입력인지 따로 구분해야함
        매수가격, 매수주식 수 - 숫자인지 검증
        날짜 - 형식검증
        */

        //입력받기
        System.out.println("주식 정보를 입력하세요");

        Nation nation = getNation();

        System.out.print("종목 명:");
        String name = kb.nextLine();

        double buyPrice = getPrice();
        double buyHoldings = getHoldings();
        LocalDate buyDate = getDate();

        //생성자를 호출해서 생성
        Stock stock = new Stock(nation,name,buyPrice,buyHoldings,buyDate);

        //portfolio에 저장
        Portfolio.addStock(stock);
    }

    @NotNull
    private LocalDate getDate() {

        while (true) {

            System.out.print("날짜를 입력하세요(예:2025-01-01):");
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

    private double getPrice() {

        while (true) {

            System.out.print("매수 가격을 입력하세요:");

            try {

                double price = kb.nextDouble();
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

            //출력 전에 환 단위 정하고 들어가야지
            System.out.print(stock.getNation().name() + "증시에서 투자중인 " + stock.getName() + "의 평균단가는 " + stock.getAveragePrice() + stock.getNation().getCurrency() + "이며 보유 주식 수는 " + stock.getHoldings() + "주 입니다. ");
            System.out.println("투자기간은 총 " + stock.howLongInvesting().getYears() + "년 " + stock.howLongInvesting().getMonths() + "개월 " + stock.howLongInvesting().getDays() + "일 입니다.");
            System.out.println("현재까지 총 매수 금액은 " + stock.getTotalPrice() + stock.getNation().getCurrency() + "입니다.");
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
                double buyPrice = getPrice();
                double buyHoldings = getHoldings();

                //Stock 객체 갱신하고 portfolio에 저장
                Stock stock = Portfolio.getPortfolio().get(0);
                stock.additionalBuy(buyPrice,buyHoldings);

                //저장함수 호출
                ExcelService.updatePreviousStock(rowNum);
                break;
            }
        }
    }
}