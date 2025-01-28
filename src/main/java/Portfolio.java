import java.util.ArrayList;
import java.util.List;

public class Portfolio {

    private static final List<Stock> portfolio = new ArrayList<>();

    //Stock 추가
    public static void addStock(Stock stock) {
        portfolio.add(stock);
    }

    //Stock 삭제(종목명으로)
    public boolean removeStock(String stockName) {
        return portfolio.removeIf(stock -> stock.getName().equals(stockName));
    }

    public static List<Stock> getPortfolio() {
        return portfolio;
    }

    //처음에 켰을 때 엑셀에서 불러올 수도 있어야함(이건 엑셀 서비스에서 ㄱㄱ)
}
