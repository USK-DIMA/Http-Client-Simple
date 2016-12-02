package http.client;

import org.apache.commons.lang3.StringUtils;

import java.awt.*;
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

    public static HttpResponse get(String url, String encoding) throws IOException, InterruptedException, ExecutionException, TimeoutException {
        return get(url, DEFAULT_PORT, encoding);
    }

    public static HttpResponse get(String url, int port, String encoding) throws IOException, InterruptedException, ExecutionException, TimeoutException {
        url = removeProtocolHttp(url);
        String host = urlToHost(url);
        url = "/" + StringUtils.substringAfter(url, "/");
        SocketChannel socketChannel = initSocket(host, port);
        HttpRequest request = createHttpRequest(host, url);
        sendRequst(socketChannel, request.toString());
        return readResponse(socketChannel, encoding);
    }

    private static SocketChannel initSocket(String host, int port) throws IOException {
        if (port == -1) {
            port = DEFAULT_PORT;
        }
        SocketChannel socketChannel = SocketChannel.open();
        SocketAddress socketAddress = new InetSocketAddress(host, port);
        socketChannel.connect(socketAddress);
        return socketChannel;
    }

    private static HttpRequest createHttpRequest(String host, String url) {
        HttpRequest request = new HttpRequest(url);
        request.addHeader("Host", host);
        request.addHeader("Connection", "Keep-alive");
        //request.addHeader("User-Agent", "Apache-HttpClient/4.3.6 (java 1.5)");
        request.addHeader("User-Agent", "HttpClient/0.1.0 (java 1.8)");
        return request;
    }

    private static void sendRequst(SocketChannel socketChannel, String message) throws IOException {
        System.out.println(message);
        ByteBuffer byteBuffer = ByteBuffer.wrap(message.getBytes(CHARSET_NAME), 0, message.getBytes(CHARSET_NAME).length);
        socketChannel.write(byteBuffer);
    }

    private static HttpResponse readResponse(SocketChannel socketChannel, String encoding) throws ExecutionException, InterruptedException, TimeoutException, IOException {
        HttpResponse response = new HttpResponse();

        String startLine = readStartLine(socketChannel);
        response.initFirstLine(startLine);

        List<HttpHeader> headers = readHeaders(socketChannel);
        response.addHeaders(headers);

        String body = readBody(socketChannel, encoding, response);

        response.setBody(body);
        return response;
    }

    private static String readStartLine(SocketChannel socketChannel) throws IOException {
        StringBuilder builder = new StringBuilder();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1);
        int read;
        boolean statusLineComlite = false;
        String statusLine = "";
        while (!statusLineComlite) {
            byteBuffer.clear();
            read = socketChannel.read(byteBuffer);
            if (read != -1) {
                builder.append(new String(byteBuffer.array(), 0, read, CHARSET_NAME));
                statusLine = builder.toString();
                statusLineComlite = statusLine.indexOf("\r\n") != -1;
            }
        }
        return StringUtils.substringBefore(statusLine, "\r\n");
    }

    private static List<HttpHeader> readHeaders(SocketChannel socketChannel) throws IOException {
        StringBuilder builder = new StringBuilder();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1);
        int read = 1;
        boolean headresIsRed = false;
        String readedMessage = "";
        while (!headresIsRed) {
            byteBuffer.clear();
            read = socketChannel.read(byteBuffer);
            if (read != -1) {
                builder.append(new String(byteBuffer.array(), 0, read, CHARSET_NAME));
                readedMessage = builder.toString();
                headresIsRed = readedMessage.indexOf("\r\n\r\n") != -1;
            }
        }
        String headers = StringUtils.substringBefore(builder.toString(), "\r\n\r\n");
        return parseHeaders(headers);
    }

    private static List<HttpHeader> parseHeaders(String headers) {
        List<HttpHeader> headrsList = new ArrayList<HttpHeader>();
        String[] headersSplit = headers.split("\r\n");
        for (int i = 0; i < headersSplit.length; i++) {
            String[] header = headersSplit[i].split(": ");
            headrsList.add(new HttpHeader(header[0], header[1]));
        }
        return headrsList;
    }

    private static String readBody(SocketChannel socketChannel, String encoding, HttpResponse response) throws IOException {
        String body;
        if (response.headerValue("Transfer-Encoding", "chunked")) {
            body = readByChunk(socketChannel, encoding);
        } else if (response.headerByKey("Content-Length") != null) {
            String lengthString = response.headerByKey("Content-Length").getValue();
            int length = Integer.valueOf(lengthString);
            body = readChunk(length, socketChannel, encoding);
        } else {
            throw new IllegalComponentStateException("");
        }
        return body;
    }

    private static String readByChunk(SocketChannel socketChannel, String encoding) throws IOException {
        StringBuilder builderBody = new StringBuilder();
        String chunkString;
        do {
            chunkString = readChunk(socketChannel, encoding);
            if(chunkString!=null) {
                builderBody.append(chunkString);
            }
        } while (chunkString != null);

        return builderBody.toString();
    }

    private static int parseChunckLength(String chunkLengthString) {
        String chunkLengthHex = chunkLengthString.replace("\r\n", "");
        if(chunkLengthHex.trim().equals("0")){
            return -1;
        }
        return Integer.parseInt(chunkLengthHex, 16);
    }

    private static String readChunk(SocketChannel socketChannel, String encoding) throws IOException {
        StringBuilder builder = new StringBuilder();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1);
        int read;
        boolean chunkLengthIsRead = false;
        String chunkLengthString = "";
        while (!chunkLengthIsRead) {
            byteBuffer.clear();
            read = socketChannel.read(byteBuffer);
            if(read!=-1) {
                builder.append(new String(byteBuffer.array(), 0, read, CHARSET_NAME));
                chunkLengthString = builder.toString();
                chunkLengthIsRead = (chunkLengthString.indexOf("\r\n") != -1 && !StringUtils.substringBeforeLast(chunkLengthString, "\r\n").equals(""));
            }
        }
        return readChunk(parseChunckLength(chunkLengthString), socketChannel, encoding);
    }

    private static String readChunk(int chunkLengthL, SocketChannel socketChannel, String encoding) throws IOException {
        if (chunkLengthL == -1) {
            return null;
        }
        StringBuilder builder = new StringBuilder(chunkLengthL);
        ByteBuffer byteBuffer;
        long read;
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

}
