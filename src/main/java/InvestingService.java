import java.time.LocalDate;
import java.util.Scanner;
import java.time.format.DateTimeFormatter;
public class InvestingService {

    Scanner kb = new Scanner(System.in);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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
        각 항목별로 입력값 유효성 검사를 구현해야함, 예를 들면 이름을 입력 받으면 엑셀에서 중복을 검사해서 이미 있다고 콘솔에 출력해주고 다시 입력받거나, 맨 처음 페이지로 가도록
        매수 가격이나 매수 주식 수는 괜찮을 것 같은데, 날짜 유효성 검사는 확실히 필요할 듯, 이건 정규표현식..? 활용해야할 듯
        nation 받는 것처럼 나머지 입력도 다 입력 유효성 검사하는 식으로 수정하면 될 듯
        */

        //입력받기
        System.out.println("주식 정보를 입력하세요");

        Nation nation = getNation();
        System.out.print("종목 명:");
        String name = kb.nextLine();
        System.out.print("매수 가격:");
        double buyPrice = kb.nextDouble();
        kb.nextLine();
        System.out.print("매수 주식 수:");
        double buyHoldings = kb.nextDouble();
        kb.nextLine();
        System.out.print("날짜를 입력하세요(예:2025-01-01):");
        String buyDateStr = kb.nextLine();
        LocalDate buyDate = LocalDate.parse(buyDateStr,formatter);

        //생성자를 호출해서 생성
        Stock stock = new Stock(nation,name,buyPrice,buyHoldings,buyDate);

        //portfolio에 저장
        Portfolio.addStock(stock);
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
                System.out.print("매수 가격:");
                double buyPrice = kb.nextDouble();
                kb.nextLine();
                System.out.print("매수 주식 수:");
                double buyHoldings = kb.nextDouble();
                kb.nextLine();

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