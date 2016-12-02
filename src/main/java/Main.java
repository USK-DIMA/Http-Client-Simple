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
        HttpResponse response = HttpClient.get("http://www.javaportal.ru/java/articles/java_http_web/article03.html", "windows-1251");
        System.out.println(response.getBody());

    }
}
