package http.client;

import static http.client.HttpConst.END_LINE;

/**
 * Created by Dmitry on 30.11.2016.
 */
public class HttpHeader {
    public static final String SEPARATOR = ": ";
    private String key;
    private String value;

    public HttpHeader(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getHeader(){
        StringBuilder stringBuilder = new StringBuilder(length());
        stringBuilder.append(key);
        stringBuilder.append(SEPARATOR);
        stringBuilder.append(value);
        stringBuilder.append(END_LINE);
        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        return key + SEPARATOR + value;
    }

    private int length() {
        return value.length() + key.length()+ SEPARATOR.length() + END_LINE.length();
    }


}
