package utility.apiRequest;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class GetKrStockPriceTest2 {

    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        // 삼성전자(005930)의 52주 최고가를 구하기 위해
        // 예시로 startTime과 endTime을 직접 지정 (YYYYMMDD 형식)
        // 실제 사용 시에는 현재 날짜를 기준으로 1년 전 날짜를 계산해 주세요.
        String symbol = "005930";
        String startTime = "20240221"; // 예: 2024년 2월 21일
        String endTime = "20250221";   // 예: 2025년 2월 21일

        fetch52WeekHigh(symbol, startTime, endTime);
    }

    private static void fetch52WeekHigh(String symbol, String startTime, String endTime) {
        // Naver Finance의 역사적 주가 데이터 엔드포인트 URL 구성
        String url = "https://api.finance.naver.com/siseJson.naver?symbol=" + symbol
                + "&requestType=1&startTime=" + startTime + "&endTime=" + endTime;

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.out.println("요청 실패: HTTP 코드 " + response.code());
                return;
            }

            String responseData = response.body().string();
            System.out.println("응답 데이터: " + responseData);

            // 응답은 JSON 형식의 배열 배열 형태로 전달돼요.
            // 첫 번째 행은 헤더이고, 이후 행은 일별 데이터입니다.
            // 헤더 예시: ["날짜", "종가", "전일비", "시가", "고가", "저가", "거래량"]
            Type listType = new TypeToken<List<List<String>>>() {}.getType();
            List<List<String>> data = gson.fromJson(responseData, listType);

            // 헤더를 제외한 데이터 행들을 순회하면서 "고가" (index 4)의 최대값을 찾습니다.
            double maxHigh = Double.MIN_VALUE;
            for (int i = 1; i < data.size(); i++) {
                List<String> row = data.get(i);
                if (row.size() > 4) {
                    // 숫자 데이터의 경우 천 단위 구분 기호(,)가 있을 수 있으므로 제거합니다.
                    String highStr = row.get(4).replaceAll(",", "");
                    try {
                        double high = Double.parseDouble(highStr);
                        if (high > maxHigh) {
                            maxHigh = high;
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("숫자 변환 오류: " + highStr);
                    }
                }
            }

            if (maxHigh == Double.MIN_VALUE) {
                System.out.println("52주 최고가 데이터를 찾을 수 없어요.");
            } else {
                System.out.println("종목 " + symbol + "의 52주 최고가: " + maxHigh);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}