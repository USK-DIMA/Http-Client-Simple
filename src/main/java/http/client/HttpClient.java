package http.client;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Created by Dmitry on 30.11.2016.
 */
public class HttpClient {

    public static final int DEFAULT_PORT = 80;
    private static final String CHARSET_NAME = "UTF-8";

    private static void sendRequst(SocketChannel socketChannel, String message) throws IOException {
        System.out.println(message);
        ByteBuffer byteBuffer = ByteBuffer.wrap(message.getBytes(CHARSET_NAME), 0, message.getBytes(CHARSET_NAME).length);
        socketChannel.write(byteBuffer);
    }

    private static HttpResponse readResponse(SocketChannel socketChannel, String encoding) throws ExecutionException, InterruptedException, TimeoutException, IOException {
        HttpResponse response = new HttpResponse();

        StringBuilder  builder = new StringBuilder();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1);
        int read = 1;
        boolean headresIsRed = false;
        String readedMessage = "";
        while (!headresIsRed) {
            byteBuffer.clear();
            read = socketChannel.read(byteBuffer);
            if(read!=-1) {
                builder.append(new String(byteBuffer.array(), 0, read, CHARSET_NAME));
                readedMessage = builder.toString();
                headresIsRed = readedMessage.indexOf("\r\n\r\n")!=-1;
            }
        }
        String headersAndStartLine = StringUtils.substringBefore(builder.toString(), "\r\n\r\n");
        String startLine = StringUtils.substringBefore(headersAndStartLine, "\r\n");
        String headers = StringUtils.substringAfter(headersAndStartLine, "\r\n");
        List<HttpHeader> responseHeaders = parseHeaders(headers);
        response.addHeaders(responseHeaders);
        response.initFirstLine(startLine);
        if(!response.headerValue("Transfer-Encoding", "chunked")) {
            throw new RuntimeException("Невозможно принять данный запрос");
        }

        //String chunkString = readChunk(readedMessage, socketChannel, encoding);
        /*String chunkLengthHex = StringUtils.substringBetween(readedMessage, "\r\n\r\n", "\r\n");

        int chunkLengthL = Integer.parseInt(chunkLengthHex, 16);
        String m = StringUtils.substringAfter(readedMessage, chunkLengthHex+"\r\n");
        chunkLengthL-= m.getBytes(CHARSET_NAME).length;*/
        //String chunkString = m + readChunk(chunkLengthL, socketChannel, encoding);

        //printSimple(socketChannel);

        StringBuilder builderBody = new StringBuilder();
        String chunkString = "";
        //builderBody.append(chunkString);
        while (chunkString !=null) {
            chunkString = readChunk(socketChannel, encoding);
            if(chunkString!=null) {
                builderBody.append(chunkString);
            }
        }
        String body = builderBody.toString();

        System.out.println(body);

        return response;
    }


    private static String readChunk(String readedMessage, SocketChannel socketChannel, String encoding) throws IOException {
        String chunkLengthHex = readedMessage.replace("\r\n", "");
        if(chunkLengthHex.trim().equals("0")){
            return null;
        }
        int chunkLengthL = Integer.parseInt(chunkLengthHex, 16);
        //String m = StringUtils.substringAfter(readedMessage, chunkLengthHex+"\r\n");
        //int bCount = m.getBytes(CHARSET_NAME).length;
        //chunkLengthL-= bCount;
        StringBuilder builderBody = new StringBuilder();
        String chunkString = readChunk(chunkLengthL, socketChannel, encoding);
        return chunkString;
    }

    private static String readChunk(SocketChannel socketChannel, String encoding) throws IOException {
        StringBuilder builder = new StringBuilder();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1);
        int read = 1;
        int sumRead = 0;
        boolean chunkLengthIsRead = false;
        String readedMessage = "";
        while (!chunkLengthIsRead) {
            byteBuffer.clear();
            read = socketChannel.read(byteBuffer);
            if(read!=-1) {
                builder.append(new String(byteBuffer.array(), 0, read, CHARSET_NAME));
                readedMessage = builder.toString();
                chunkLengthIsRead = (readedMessage.indexOf("\r\n")!=-1 && !StringUtils.substringBeforeLast(readedMessage, "\r\n").equals(""));
            }
        }
        return readChunk(readedMessage, socketChannel, encoding);
    }

    private static String readChunk(int chunkLengthL, SocketChannel socketChannel, String encoding) throws IOException {
        StringBuilder builder = new StringBuilder(chunkLengthL);
        ByteBuffer byteBuffer;
        long read = 1;
        long sumRead = 0;
        while (sumRead<chunkLengthL) {
            byteBuffer = ByteBuffer.allocate((int)(chunkLengthL-sumRead));
            read = socketChannel.read(byteBuffer);
            if(read!=-1) {
                sumRead+=read;
                builder.append(new String(byteBuffer.array(), encoding));
            }
        }
        return builder.toString();
    }

    private static List<HttpHeader> parseHeaders(String headers) {
        List<HttpHeader> headrsList= new ArrayList<HttpHeader>();
        String[] headersSplit = headers.split("\r\n");
        for(int i=0; i<headersSplit.length; i++){
            String[] header = headersSplit[i].split(": ");
            headrsList.add(new HttpHeader(header[0], header[1]));
        }
        return headrsList;
    }


    /**
     * Удаляет префикс http://, если таковой имеется
     * @param url
     * @return
     */
    private static String removeProtocolHttp(String url) {
        if(StringUtils.startsWith(url, "http://")) {
            return StringUtils.substringAfter(url, "http://");
        }
        return url;
    }

    private static String urlToHost(String url) {
        return StringUtils.substringBefore(url, "/");
    }

    private static SocketChannel initSocket(String host, int port) throws IOException {
        if(port == -1) {
            port = DEFAULT_PORT;
        }
        SocketChannel socketChannel = SocketChannel.open();
        SocketAddress socketAddress = new InetSocketAddress(host, port);
        socketChannel.connect(socketAddress);
        return socketChannel;
    }



    public static HttpResponse get(String url, String encoding) throws IOException, InterruptedException, ExecutionException, TimeoutException {
        return get(url, DEFAULT_PORT, encoding);
    }
    public static HttpResponse getSimple(String url, String encoding) throws IOException, InterruptedException, ExecutionException, TimeoutException {
        url = removeProtocolHttp(url);
        String host = urlToHost(url);
        url = "/"+StringUtils.substringAfter(url, "/");
        SocketChannel socketChannel = initSocket(host, 80);
        HttpRequest request = new HttpRequest(url);
        request.addHeader("Host", host);
        request.addHeader("Connection", "Keep-alive");
        request.addHeader("User-Agent", "Apache-HttpClient/4.3.6 (java 1.5)");
        sendRequst(socketChannel, request.toString());
        printSimple(socketChannel);

        return null;
    }

    private static void printSimple(SocketChannel socketChannel) throws IOException {
        StringBuilder  builder = new StringBuilder();
        ByteBuffer byteBuffer = ByteBuffer.allocate(16384);
        int read = 1;
        String readedMessage = "";
        while (read>0) {
            byteBuffer.clear();
            read = socketChannel.read(byteBuffer);
            if(read!=-1) {
                System.out.println(new String(byteBuffer.array(), 0, read, "UTF-8"));
            }
        }
        //System.out.println(builder.toString());
    }

    public static HttpResponse get(String url, int port, String encoding) throws IOException, InterruptedException, ExecutionException, TimeoutException {
        url = removeProtocolHttp(url);
        String host = urlToHost(url);
        url = "/"+StringUtils.substringAfter(url, "/");
        SocketChannel socketChannel = initSocket(host, port);
        HttpRequest request = new HttpRequest(url);
        request.addHeader("Host", host);
        request.addHeader("Connection", "Keep-alive");
        request.addHeader("User-Agent", "Apache-HttpClient/4.3.6 (java 1.5)");
        sendRequst(socketChannel, request.toString());
        return readResponse(socketChannel, encoding);
    }
}
