package utility.apiRequest;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

public class TreasuryTest {

    //외부에 공개되면X
    private static final String API_KEY = "";

    //미국 10년물 국채를 의미
    private static final String SERIES_ID = "DGS10";
    private static final String URL = "https://api.stlouisfed.org/fred/series/observations?series_id=" + SERIES_ID + "&api_key=" + API_KEY + "&file_type=json"
    public static void main(String[] args) {

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(URL).build();

        try (Response response = client.newCall(request).execute()) {

            if (response.isSuccessful() && response.body() != null) {

                String jsonData = response.body().string();
                JsonObject jsonObject = JsonParser.parseString(jsonData).getAsJsonObject();

                //
                JsonArray observations = jsonObject.getAsJsonArray("observations");

                if (observations != null && observations.size() > 0) {

                    JsonObject latestObservation = observations.get(observations.size()- 1).getAsJsonObject();
                    String data = latestObservation.get("date").getAsString();
                    String rate = latestObservation.get("value").getAsString();
                    System.out.println(data + "의 미국 10년물 국채 금리" + rate + "%");
                }
                else {
                    System.out.println("데이터가 없습니다.");
                }
            }
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }
}
