package utility.apiRequest;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class GetUsStockPriceTest2 {

    private static final String API_KEY = "";

    //그때그때 생성하는게 아니라 미리 만들어놓는 거 괜찮은 듯
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) {
        String symbol = "AAPL";  // 미국 주식 티커 예시
        fetchMonthly52WeekHigh(symbol);
    }

    private static void fetchMonthly52WeekHigh(String symbol) {
        // Alpha Vantage의 월별 데이터 엔드포인트 사용
        String url = "https://www.alphavantage.co/query?function=TIME_SERIES_MONTHLY&symbol="
                + symbol + "&apikey=" + API_KEY;

        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.out.println("요청 실패: HTTP 코드 " + response.code());
                return;
            }
            String responseData = response.body().string();
            JsonObject jsonObject = gson.fromJson(responseData, JsonObject.class);
            JsonObject monthlyTimeSeries = jsonObject.getAsJsonObject("Monthly Time Series");
            if (monthlyTimeSeries == null) {
                System.out.println("Monthly Time Series 데이터가 없습니다.");
                return;
            }

            // 현재 날짜 기준 52주 전(약 1년 전)을 계산
            LocalDate oneYearAgo = LocalDate.now().minusWeeks(52);
            double maxHigh = Double.MIN_VALUE;

            System.out.println("monthlyTimeSeries.entrySet() = " + monthlyTimeSeries.entrySet());
            // 월별 데이터의 각 날짜(키)는 "yyyy-MM-dd" 형식임
            for (Map.Entry<String, com.google.gson.JsonElement> entry : monthlyTimeSeries.entrySet()) {

                String dateStr = entry.getKey();
                LocalDate date = LocalDate.parse(dateStr, formatter);
                // 52주(1년) 이전의 데이터는 건너뛰기
                if (date.isBefore(oneYearAgo)) continue;

                JsonObject monthlyData = entry.getValue().getAsJsonObject();
                String highStr = monthlyData.get("2. high").getAsString();
                double high = Double.parseDouble(highStr);
                if (high > maxHigh) {
                    maxHigh = high;
                }
            }

            if (maxHigh == Double.MIN_VALUE) {
                System.out.println("최근 52주(월별 기준) 데이터가 충분하지 않습니다.");
            } else {
                System.out.println(symbol + "의 최근 52주(월별 데이터 기준) 최고가: " + maxHigh);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
