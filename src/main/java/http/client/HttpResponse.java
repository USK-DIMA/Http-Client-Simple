package http.client;


/**
 * Created by Dmitry on 02.12.2016.
 */
public class HttpResponse extends Http {

    private int statusCode;
    private String statusMessage;

    public void initFirstLine(String firstLine){
        String[] fl  = firstLine.split(" ");
        version = fl[0];
        statusCode = Integer.valueOf(fl[1]);
        statusMessage = fl[2];
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
}
