
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Dmitry on 30.11.2016.
 */
public class ApacheHttpClientTest {
    public static void main(String[] args) throws IOException {
        HttpClient client = new DefaultHttpClient();
        //HttpGet request = new HttpGet("http://restUrl");
        HttpGet request = new HttpGet("http://www.javaportal.ru/java/articles/java_http_web/article03.html");
        System.out.println(request.toString());
        HttpResponse response = client.execute(request);
/*        BufferedReader rd = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
        String line = "";
        while ((line = rd.readLine()) != null) {
            System.out.println(line);
        }*/
    }
}
