import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CurrencyTest2 {

    private static final String API_URL = "https://open.er-api.com/v6/latest/USD";

    public static void main(String[] args) {

        OkHttpClient client = new OkHttpClient();

        //api호출에 담을 내용 객체 생성
        Request request = new Request.Builder().url(API_URL).build();

        try (Response response = client.newCall(request).execute()) {

            if (response.isSuccessful() && response.body() != null) {

                //reponse의 body만 가져와서 그걸 string화
                String responseBody = response.body().string();
                JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();

                //원/달러 환율 추출
                JsonObject rates = jsonObject.getAsJsonObject("rates");
                int usdToKrw = rates.get("KRW").getAsInt();

                System.out.println("현재 원 달러 환율:" + usdToKrw);
            }
            else {
                System.out.println("API 호출 실패: " + response.code());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
