package utility.apiRequest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CurrencyTest2 {

    private static final String API_URL = "https://open.er-api.com/v6/latest/USD";

    public static void main(String[] args) {

        OkHttpClient client = new OkHttpClient();

        //Request.Builder 객체 만들어주고, 거기에 url 넣어주고, 불변객체화
        Request request = new Request.Builder().url(API_URL).build();

        //위에서 만든 내용을 바탕으로 Call 객체 생성(실제 요청을 수행할 수 있는 단위), 요청 실행
        try (Response response = client.newCall(request).execute()) {

            //성공적으로 잘 받아왔으면
            if (response.isSuccessful() && response.body() != null) {

                //reponse의 body만 가져와서 그걸 string화(일종의 감싸 져 있는 것을 해체)
                String responseBody = response.body().string();
                //String화만 하고 끝내면, key를 이용해서 value를 꺼낼 수 없음, 그래서 자바가 이해할 수 있는 Json 객체로 변환해줘야함
                JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                System.out.println("jsonObject = " + jsonObject);

                //원/달러 환율 추출
                JsonObject rates = jsonObject.getAsJsonObject("rates");
                double usdToKrw = rates.get("KRW").getAsDouble();

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
