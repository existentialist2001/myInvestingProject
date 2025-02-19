package utility.apiRequest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CurrencyTest {
    private static final String API_URL = "https://open.er-api.com/v6/latest/USD";

    public static void main(String[] args) {
        OkHttpClient client = new OkHttpClient();

        // API 호출
        Request request = new Request.Builder()
                .url(API_URL)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                // JSON 응답 파싱
                System.out.println("json 뜯어보기 " + response.body());
                String responseBody = response.body().string();
                System.out.println("responseBody = " + responseBody);
                JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                System.out.println("jsonObject = " + jsonObject);

                // 원/달러 환율 추출
                JsonObject rates = jsonObject.getAsJsonObject("rates");
                double usdToKrw = rates.get("KRW").getAsDouble();

                System.out.println("현재 원/달러 환율: " + usdToKrw);
            } else {
                System.out.println("API 호출 실패: " + response.code());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}