package utility.apiRequest;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class GetKrStockPirceTest {

    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();

    public static void main(String[] args) {

        fetchStockPrice("466920");
    }

    private static void fetchStockPrice(String code) {

        // Naver Finance의 실시간 주가 API 엔드포인트
        String url = "https://polling.finance.naver.com/api/realtime?query=SERVICE_ITEM:" + code;

        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.out.println("요청 실패: HTTP 코드 " + response.code());
                return;
            }

            String responseData = response.body().string();
            System.out.println("응답: " + responseData);

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
                            System.out.println("종목 " + code + "의 현재 가격: " + price);
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
    }
}
