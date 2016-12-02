import http.client.HttpClient;
import http.client.HttpResponse;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Created by Dmitry on 30.11.2016.
 */
public class Main {

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException, TimeoutException {
        String url = "http://www.javaportal.ru/java/articles/java_http_web/article03.html";
        String encoding = "windows-1251";
        HttpResponse response = HttpClient.get(url, encoding);

        System.out.println(response.getStatusCode());
        System.out.println(response.getStatusMessage());
        System.out.println(response.getVersion());
        System.out.println(response.getBody());

    }
}
