package com.ssd8.socket.http;

import java.io.*;
import java.net.Socket;

/**
 * Class <em>HttpClient</em> is a class representing a simple HTTP client.
 *
 * @author wben
 */

public class HttpClient {

    /**
     * default HTTP port is port 80
     */
    private static int port = 80;

    /**
     * Allow a maximum buffer size of 8192 bytes
     */
    private static int buffer_size = 8192;

    /**
     * Response is stored in a byte array.
     */
    private byte[] buffer;

    /**
     * My socket to the world.
     */
    Socket socket = null;

    /**
     * Default port is 80.
     */
    private static final int PORT = 80;

    /**
     * Output stream to the socket.
     */
    BufferedOutputStream ostream = null;

    /**
     * Input stream from the socket.
     */
    BufferedInputStream istream = null;

    /**
     * StringBuffer storing the header
     */
    private StringBuffer header = null;
    private StringBuffer putheader = null;
    /**
     * StringBuffer storing the response.
     */
    private StringBuffer response = null;

    /**
     * String to represent the Carriage Return and Line Feed character sequence.
     */
    static private String CRLF = "\r\n";

    /**
     * HttpClient constructor;
     */
    public HttpClient() {
        buffer = new byte[buffer_size];
        header = new StringBuffer();
        putheader = new StringBuffer();
        response = new StringBuffer();
    }

    /**
     * <em>connect</em> connects to the input host on the default http port --
     * port 80. This function opens the socket and creates the input and output
     * streams used for communication.
     */
    public void connect(String host) throws Exception {

        /**
         * Open my socket to the specified host at the default port.
         */
        socket = new Socket(host, PORT);

        /**
         * Create the output stream.
         */
        ostream = new BufferedOutputStream(socket.getOutputStream());

        /**
         * Create the input stream.
         */
        istream = new BufferedInputStream(socket.getInputStream());
    }

    /**
     * <em>processGetRequest</em> process the input GET request.
     */
    public void processGetRequest(String request) throws Exception {
        /**
         * Send the request to the server.
         */
        request += CRLF + CRLF;
        buffer = request.getBytes();
        ostream.write(buffer, 0, request.length());
        ostream.flush();
        /**
         * waiting for the response.
         */
        processResponse();
    }

    /**
     * <em>processPutRequest</em> process the input PUT request.
     */
    public void processPutRequest(String request) throws Exception {
        //=======start your job here============//
        String fileName = "./face.jpg";
        String[] split = request.split(" ", 3);
        String putFileName = split[1];
        File putFile = new File(fileName);
        putheader.append("PUT " + putFileName + " HTTP/1.0" + CRLF);
        FileType fileType = new FileType(fileName);
        putheader.append("Content-Type: " + fileType.getType(fileName) + CRLF);
        putheader.append("Content-Length: " + putFile.length() + CRLF + CRLF);

        ostream.write(putheader.toString().getBytes(), 0, putheader.length());
        putFile(fileName);
        ostream.flush();
        /**
         * waiting for the response.
         */
        processResponse();
        //=======end of your job============//
    }


    /**
     * put 文件的传输过程
     * @param fileName
     * @throws IOException
     */
    public void putFile(String fileName) throws IOException {
        File file = new File(fileName);
        if (file.exists()) {
            FileInputStream fis = new FileInputStream(file);
            int fileLength = fis.available();
            int position = 0;
            int byteRead = 0;
            while (position < fileLength) {
                byteRead = fis.read(buffer);
                ostream.write(buffer, 0, byteRead);
                position += byteRead;
            }
            fis.close();
        }
    }

    /**
     * <em>processResponse</em> process the server response.
     */
    public void processResponse() throws Exception {
        int last = 0, c = 0;
        /**
         * Process the header and add it to the header StringBuffer.
         */
        boolean inHeader = true; // loop control
        while (inHeader && ((c = istream.read()) != -1)) {
            switch (c) {
                case '\r':
                    break;
                case '\n':
                    if (c == last) {
                        inHeader = false;
                        break;
                    }
                    last = c;
                    header.append("\n");
                    break;
                default:
                    last = c;
                    header.append((char) c);
            }
        }

        /**
         * Read the contents and add it to the response StringBuffer.
         */
        int b;
        while ((b = istream.read(buffer)) != -1) {
            response.append(new String(buffer, "iso-8859-1")).substring(0,b);
        }
    }

    /**
     * Get the response header.
     */
    public String getHeader() {
        return header.toString();
    }

    /**
     * Get the server's response.
     */
    public String getResponse() {
        return response.toString();
    }

    /**
     * Close all open connections -- sockets and streams.
     */
    public void close() throws Exception {
        socket.close();
        istream.close();
        ostream.close();
    }
}
