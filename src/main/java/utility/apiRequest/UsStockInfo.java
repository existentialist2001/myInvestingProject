package utility.apiRequest;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class UsStockInfo {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String API_KEY = "";
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();

    //현재가를 가져오기
    public static double getCurrentStockPrice(String ticker) {

        // Alpha Vantage GLOBAL_QUOTE API URL 구성
        String url = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=" + ticker + "&apikey=" + API_KEY;

        // OkHttp Request 객체 생성
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.out.println("[" + ticker + "] 요청 실패 - HTTP 코드: " + response.code());
                return -1;
            }

            // 응답을 문자열로 받아오기
            String responseData = response.body().string();

            // Gson을 사용하여 JSON 파싱
            JsonObject jsonObject = gson.fromJson(responseData, JsonObject.class);
            JsonObject globalQuote = jsonObject.getAsJsonObject("Global Quote");

            if (globalQuote != null && globalQuote.has("05. price")) {
                String price = globalQuote.get("05. price").getAsString();
                return Double.parseDouble(price);
            } else {
                System.out.println(ticker + " 데이터가 없거나 형식이 변경되었어요.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    //52주 최고가를 가져오기
    public static double getHighestStockPrice(String ticker) {

        // Alpha Vantage의 월별 데이터 엔드포인트 사용
        String url = "https://www.alphavantage.co/query?function=TIME_SERIES_MONTHLY&symbol=" + ticker + "&apikey=" + API_KEY;

        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.out.println("요청 실패: HTTP 코드 " + response.code());
                return -1;
            }
            String responseData = response.body().string();
            JsonObject jsonObject = gson.fromJson(responseData, JsonObject.class);
            JsonObject monthlyTimeSeries = jsonObject.getAsJsonObject("Monthly Time Series");
            if (monthlyTimeSeries == null) {
                System.out.println("Monthly Time Series 데이터가 없습니다.");
                return -1;
            }

            // 현재 날짜 기준 52주 전(약 1년 전)을 계산
            LocalDate oneYearAgo = LocalDate.now().minusWeeks(52);
            double maxHigh = Double.MIN_VALUE;

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
                return maxHigh;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}
