package http.client;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Dmitry on 02.12.2016.
 */
public class Http {

    protected List<HttpHeader> headers = new ArrayList<>();

    protected String version;

    public int addHeader(HttpHeader header) {
        headers.add(header);
        return headers.size();
    }

    public int addHeader(String key, String value) {
        return addHeader(new HttpHeader(key, value));
    }

    public void addHeaders(List<HttpHeader> headers) {
        this.headers.addAll(headers);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public HttpHeader headerByKey(String key){
        for(int i=0 ;i<headers.size(); i++) {
            if(headers.get(i).getKey().equals(key)){
                return headers.get(i);
            }
        }
        return null;
    }

    public boolean headerValue(String key, String value){
        HttpHeader header = headerByKey(key);
        if(header!=null){
            return header.getValue().equals(value);
        }
        return false;
    }
}
