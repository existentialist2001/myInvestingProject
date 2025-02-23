package utility.apiRequest;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class NaverMonthlyHighCalculator {

    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        // 네이버 파이낸스 월별 데이터 API URL (예시)
        String url = "https://m.stock.naver.com/front-api/external/chart/domestic/info?symbol=005930&requestType=1&startTime=20240531&endTime=20250223&timeframe=month";
        fetchMonthlyHigh(url);
    }

    private static void fetchMonthlyHigh(String url) {

        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.out.println("요청 실패: HTTP 코드 " + response.code());
                return;
            }
            String responseData = response.body().string();
            System.out.println("응답 데이터: " + responseData);

            // 응답 데이터는 JSON 배열 형태로 전달됩니다.
            // 첫 번째 행은 헤더 (예: ['날짜', '시가', '고가', ...])이고,
            // 이후 행들은 실제 월별 데이터입니다.
            JsonArray dataArray = gson.fromJson(responseData, JsonArray.class);
            if (dataArray == null || dataArray.size() < 2) {
                System.out.println("데이터가 충분하지 않습니다.");
                return;
            }

            // 첫 번째 배열(헤더)은 건너뛰고, 각 행의 인덱스 2 ("고가") 값을 비교하여 최대값을 찾습니다.
            double maxHigh = Double.MIN_VALUE;
            for (int i = 1; i < dataArray.size(); i++) {
                JsonElement element = dataArray.get(i);
                if (!element.isJsonArray()) continue;
                JsonArray row = element.getAsJsonArray();
                if (row.size() < 3) continue;  // 인덱스 2가 존재하는지 확인

                double highValue = row.get(2).getAsDouble();
                if (highValue > maxHigh) {
                    maxHigh = highValue;
                }
            }

            System.out.println("52주(월별 기준) 최고가: " + maxHigh);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

