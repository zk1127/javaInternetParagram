package com.ssd8.socket.http;


import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * @Author:zengkang
 * @date:Create in 15:05 2017/11/27
 */
public class ServerHandler implements Runnable {
    private Socket socket;
    private String CRLF = "\r\n";
    private byte[] buffer = null;
    private final String fileBase = "./ServerFiles";
    private final File ServerRoot = new File(fileBase);
    private BufferedOutputStream bos = null;
    private BufferedInputStream bis = null;
    private StringBuffer header;
    private StringBuffer responseHeader;

    /**
     * constructor
     *
     * @param socket
     */
    public ServerHandler(Socket socket) throws IOException {
        this.socket = socket;
        bis = new BufferedInputStream(socket.getInputStream());
        bos = new BufferedOutputStream(socket.getOutputStream());
        header = new StringBuffer();
        responseHeader = new StringBuffer();
        buffer = new byte[8192];
    }

    /**
     * 处理客户端发来的请求
     *
     * @throws Exception IO异常
     */
    public String[] processSegment() throws Exception {
        int last = 0, c = 0;
        /**
         * Process the header and add it to the header StringBuffer.
         */
        boolean inHeader = true; // loop control
        while (inHeader && ((c = bis.read()) != -1)) {
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
        return header.toString().replace(CRLF, "\\s").split("\\s");
    }

    /**
     * Get the header.
     */
    public String getHeader() {
        return header.toString();
    }


    /**
     * function for answer the client
     */
    @Override
    public void run() {
        try {
            String[] request = processSegment();
            String header = getHeader();
            System.out.println(header);
            if (header.startsWith("GET")) { //get 请求
                doGET(header, bos);
            } else if (header.startsWith("PUT")) { // put请求
                long length = 0;
                for (int i = 0; i < request.length; i++) {
                    if (request[i].equals("Content-Length:")) {
                        length = Long.parseLong(request[i + 1]);
                        break;
                    }
                }
                doPUT(request[1], bos, length);
            } else {
                doIllegalRequest(bos);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                    bos.close();
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 处理其他请求
     */
    private void doIllegalRequest(BufferedOutputStream bos) throws IOException {
        responseHeader.append("HTTP/1.0 405 Method Not Allowed" + CRLF + CRLF);
        bos.write(responseHeader.toString().getBytes(), 0, responseHeader.length());
        bos.flush();
    }

    /**
     * 处理get请求的方法
     *
     * @param request
     * @param bos
     */
    private void doGET(String request, BufferedOutputStream bos) throws IOException, InterruptedException {
        String[] split = request.split(" ", 3);
        String fileName = split[1];
        String getFileName;
        if (fileName.equals("/")) {
            getFileName = ServerRoot + "/index.html";
        } else {
            getFileName = ServerRoot + fileName;
        }
        File file = new File(getFileName);
        if (file.exists()) {
            long fileSize = file.length();//得到文件大小
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));

            FileType fileType = new FileType(getFileName);
            responseHeader.append("HTTP/1.0 " + 200 + " OK" + CRLF);
            responseHeader.append("Server: MyHttpServer/1.0" + CRLF);
            responseHeader.append("Content-Type: " + fileType.getType(getFileName) + CRLF);
            responseHeader.append("Content-Length: " + fileSize + CRLF + CRLF);
            bos.write(responseHeader.toString().getBytes(), 0, responseHeader.length());
            bos.flush();
            int position = 0;
            int bytesRead = 0;
            while (position < fileSize) {
                bytesRead = bis.read(buffer);
                bos.write(buffer, 0, bytesRead);
                position += bytesRead;
                Thread.sleep(1);
                bos.flush();
            }
        } else {
            responseHeader.append("HTTP/1.0 404 NOT Found" + CRLF);
            responseHeader.append("Server: MyHttpServer/1.0" + CRLF + CRLF);
            buffer = responseHeader.toString().getBytes();
            bos.write(buffer, 0, buffer.length);
            bos.flush();
        }
    }

    /**
     * 处理put请求的方法
     *
     * @param request
     * @param bos
     * @param length
     */
    private void doPUT(String request, BufferedOutputStream bos, long length) throws Exception {

        String putFileName = fileBase + request;
        File putFile = new File(putFileName);
        FileType fileType = new FileType(putFileName);
        if (putFile.exists()){
            getPutFile(putFile,length);

            responseHeader.append("HTTP/1.0 " + "200" + " OK" + CRLF);
            responseHeader.append("Server: MyHttpServer/1.0" + CRLF);
            responseHeader.append("Content-Type: " + fileType.getType(putFileName) + CRLF);
            responseHeader.append("Content-Length: " + length + CRLF + CRLF);

        } else{
            getPutFile(putFile,length);
            responseHeader.append("HTTP/1.0 " + "201" + " Created" + CRLF);
            responseHeader.append("Server: MyHttpServer/1.0" + CRLF);
            responseHeader.append("Content-Type: " + fileType.getType(putFileName) + CRLF);
            responseHeader.append("Content-Length: " + length + CRLF + CRLF);
        }
        bos.write(responseHeader.toString().getBytes(), 0, responseHeader.length());
        bos.flush();
    }

    /**
     * 得到客户端put的文件
     * @param putFile put过来的文件
     * @param length put的文件的大小
     * @throws Exception IO异常
     */
    private void getPutFile(File putFile, long length) throws Exception {
        FileOutputStream outFile = new FileOutputStream(putFile);
        int position = 0;
        int bytesRead = 0;
        while (position < length) {
            bytesRead = bis.read(buffer);
            outFile.write(buffer, 0, bytesRead);
            position += bytesRead;
        }
        outFile.flush();
        outFile.close();
    }
    /**
     * 找出文件夹所有文件的方法
     *
     * @param dir 文件名
     * @return 文件夹中的所有文件
     */
    private ArrayList<File> getFilesFromDirectory(File dir) {
        ArrayList<File> files = new ArrayList<>();
        File[] allFiles = dir.listFiles();
        for (File file : allFiles) {
            if (file.isDirectory()) {
                files.addAll(getFilesFromDirectory(file));
            } else if (file.isFile()) {
                files.add(file);
            }
        }
        return files;
    }


}
