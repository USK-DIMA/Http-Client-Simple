package http.client;

import static http.client.HttpConst.END_LINE;

/**
 * Created by Dmitry on 30.11.2016.
 */
public class HttpRequest extends Http{

    public static final String DEFAULT_METHOD = "GET";
    public static final String DEFAULT_VERSION = "HTTP/1.1";

    private String method;
    private String url;
    private String body;


    public HttpRequest(String url) {
        this(DEFAULT_METHOD, url, DEFAULT_VERSION);
    }

    public HttpRequest(String method, String url, String version) {
        this.method = method;
        this.url = url;
        this.version = version;
    }


    public String getFirstLine() {
        return method + " " + url + " " + version + END_LINE;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getBody() {
        if(body == null){
            return END_LINE;
        } else {
            return body+END_LINE;
        }
    }

    @Override
    public String toString() {
        final String[] request = {getFirstLine()};
        headers.stream().forEach(h -> request[0] += h.getHeader());
        return request[0]+getBody();
    }
}
