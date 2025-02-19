package utility.apiRequest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.math.BigDecimal;
import java.math.RoundingMode;

//문제가, 달러 환율 가져오는 코드나 엔화 환율 가져오는 코드나 사실상 똑같아서 중복임, 개선 필요
public class Currency {

    private static final String USD_API_URL = "https://open.er-api.com/v6/latest/USD";
    private static final String JPY_API_URL = "https://open.er-api.com/v6/latest/JPY";

    public static double getWonDollarExRate() {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url(USD_API_URL).build();

        try (Response response = client.newCall(request).execute()) {

            if (response.isSuccessful() && response.body() != null) {

                String responseBody = response.body().string();
                JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();

                JsonObject rates = jsonObject.getAsJsonObject("rates");
                double usdToKrw = rates.get("KRW").getAsDouble();
                BigDecimal bd = BigDecimal.valueOf(usdToKrw).setScale(2,RoundingMode.HALF_UP);
                return bd.doubleValue();
        }
            else {
                System.out.println("API 호출 실패: " + response.code());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return -1.0;
    }

    public static double getWonYenExRate() {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url(JPY_API_URL).build();

        try (Response response = client.newCall(request).execute()) {

            if (response.isSuccessful() && response.body() != null) {

                String responseBody = response.body().string();
                JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();

                JsonObject rates = jsonObject.getAsJsonObject("rates");
                double jpyToKrw = rates.get("KRW").getAsDouble();
                BigDecimal bd = BigDecimal.valueOf(jpyToKrw).setScale(2,RoundingMode.HALF_UP);
                return bd.doubleValue();
            }
            else {
                System.out.println("API 호출 실패: " + response.code());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return -1.0;
    }
}
