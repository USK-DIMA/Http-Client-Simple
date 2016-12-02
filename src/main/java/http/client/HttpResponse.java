package http.client;


import org.apache.commons.lang3.StringUtils;

/**
 * Created by Dmitry on 02.12.2016.
 */
public class HttpResponse extends Http {

    private int statusCode;
    private String statusMessage;

    public void initFirstLine(String firstLine){
        version = StringUtils.substringBefore(firstLine, " ");
        firstLine = StringUtils.substringAfter(firstLine, " ");
        statusCode = Integer.valueOf(StringUtils.substringBefore(firstLine, " "));
        statusMessage = StringUtils.substringAfter(firstLine, " ");
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getStatusLine() {
        return version + " " + statusCode + " " + statusMessage;
    }

}
