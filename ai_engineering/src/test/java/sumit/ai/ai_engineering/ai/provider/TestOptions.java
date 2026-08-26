package sumit.ai.ai_engineering.ai.provider;
import com.openai.core.ClientOptions;
import com.openai.credential.BearerTokenCredential;
import org.springframework.ai.openai.http.okhttp.SpringAiOpenAiHttpClient;
public class TestOptions {
  public static void main(String[] args) {
      try {
          SpringAiOpenAiHttpClient httpClient = SpringAiOpenAiHttpClient.builder().build();
          ClientOptions b1 = ClientOptions.builder().httpClient(httpClient).apiKey("dummy").build();
          System.out.println("APIKEY SUCCESS");
      } catch (Exception e) {
          System.out.println("APIKEY FAILED: " + e.getMessage());
      }
      try {
          SpringAiOpenAiHttpClient httpClient = SpringAiOpenAiHttpClient.builder().build();
          ClientOptions b2 = ClientOptions.builder().httpClient(httpClient).credential(BearerTokenCredential.create("dummy")).build();
          System.out.println("CREDENTIAL SUCCESS");
      } catch (Exception e) {
          System.out.println("CREDENTIAL FAILED: " + e.getMessage());
      }
  }
}
