package utility.apiRequest;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class GetUsStockPriceTest {

    private static final String API_KEY = "";

    //그때그때 생성하는게 아니라 미리 만들어놓는 거 괜찮은 듯
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();

    public static void main(String[] args) {


        //이게 가능하도록 하려면 티커를 알고있어야함
        fetchGlobalQuote("AAPL");
        fetchGlobalQuote("TSM");
        fetchGlobalQuote("BRK.b");
    }

    private static void fetchGlobalQuote(String symbol) {
        // Alpha Vantage GLOBAL_QUOTE API URL 구성
        String url = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=" + symbol + "&apikey=" + API_KEY;

        // OkHttp Request 객체 생성
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.out.println("[" + symbol + "] 요청 실패 - HTTP 코드: " + response.code());
                return;
            }

            // 응답을 문자열로 받아오기
            String responseData = response.body().string();
            System.out.println("[" + symbol + "] 응답: " + responseData);

            // Gson을 사용하여 JSON 파싱
            JsonObject jsonObject = gson.fromJson(responseData, JsonObject.class);
            JsonObject globalQuote = jsonObject.getAsJsonObject("Global Quote");

            if (globalQuote != null && globalQuote.has("05. price")) {
                String price = globalQuote.get("05. price").getAsString();
                System.out.println(symbol + " 현재 가격: " + price);
            } else {
                System.out.println(symbol + " 데이터가 없거나 형식이 변경되었어요.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
