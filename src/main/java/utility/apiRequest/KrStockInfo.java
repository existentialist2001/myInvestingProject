package utility.apiRequest;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import utility.UtilityService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class KrStockInfo {

    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();

    public static double getCurrentStockPrice(String ticker)
    {
        // Naver Finance의 실시간 주가 API 엔드포인트
        String url = "https://polling.finance.naver.com/api/realtime?query=SERVICE_ITEM:" + ticker;

        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.out.println("요청 실패: HTTP 코드 " + response.code());
                return -1;
            }

            String responseData = response.body().string();

            // Gson을 사용해 JSON 파싱하기
            JsonObject jsonObject = gson.fromJson(responseData, JsonObject.class);
            JsonObject result = jsonObject.getAsJsonObject("result");
            if (result != null) {
                JsonArray areas = result.getAsJsonArray("areas");
                if (areas != null && areas.size() > 0) {
                    JsonObject firstArea = areas.get(0).getAsJsonObject();
                    JsonArray datas = firstArea.getAsJsonArray("datas");
                    if (datas != null && datas.size() > 0) {
                        JsonObject data = datas.get(0).getAsJsonObject();
                        // 주가 정보는 일반적으로 "cv" (current value) 항목에 있어요.
                        if (data.has("nv")) {
                            String price = data.get("nv").getAsString();
                            return Double.parseDouble(price);
                        } else {
                            System.out.println("가격 정보를 찾을 수 없어요.");
                        }
                    } else {
                        System.out.println("datas 항목이 없어요.");
                    }
                } else {
                    System.out.println("areas 항목이 없어요.");
                }
            } else {
                System.out.println("result 항목이 없어요.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static double getHighestStockPrice(String ticker) {

        String url = "https://m.stock.naver.com/front-api/external/chart/domestic/info?symbol=" + ticker + "&requestType=1&startTime=20240531&endTime=20250223&timeframe=month";
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.out.println("요청 실패: HTTP 코드 " + response.code());
                return -1;
            }
            String responseData = response.body().string();

            // 응답 데이터는 JSON 배열 형태로 전달됩니다.
            // 첫 번째 행은 헤더 (예: ['날짜', '시가', '고가', ...])이고,
            // 이후 행들은 실제 월별 데이터입니다.
            JsonArray dataArray = gson.fromJson(responseData, JsonArray.class);
            if (dataArray == null || dataArray.size() < 2) {
                System.out.println("데이터가 충분하지 않습니다.");
                return -1;
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

            return maxHigh;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    //test
    public static void main(String[] args) {

        double currentPrice = getCurrentStockPrice("466920");
        double highPrice = getHighestStockPrice("466920");
        double percentage = UtilityService.calculateHowMuchFall(highPrice,currentPrice);
        System.out.println("currentPrice = " + currentPrice);
        System.out.println("highPrice = " + highPrice);
        System.out.println("percentage = " + percentage);
    }
}
