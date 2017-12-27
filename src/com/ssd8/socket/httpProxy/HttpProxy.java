package com.ssd8.socket.httpProxy;

import java.io.*;
import java.net.Socket;
import java.util.StringTokenizer;

/**
 * 多线程的handler类，处理每个请求
 * @Author:zengkang
 * @date:Create in 14:22 2017/12/9
 */
public class HttpProxy implements Runnable {
    private Socket socket;
    private BufferedOutputStream bos = null;//与要求服务器的输出字节流
    private BufferedInputStream bis = null;//与要求服务器的输入字节流
    private DataInputStream inStream = null;//与要求客户端的输出字节流
    private DataOutputStream outStream = null;//与要求客户端的输入字节流
    private String CRLF = "\r\n";

    /**
     * 构造函数
     * @param socket
     */
    public HttpProxy(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            outStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                inStream = new DataInputStream(socket.getInputStream());
                String request = inStream.readUTF();
                StringTokenizer st = new StringTokenizer(request);
                if (st.countTokens() < 2 || !request.startsWith("GET")) {//一旦不是正确的请求头部，或者不是get方法，返回400错误
                    outStream.writeBytes("HTTP/1.1 400 Bad request! \n");
                    break;
                } else {
                    String method = st.nextToken();
                    String url = st.nextToken();//得到请求的链接
                    getObject(url, bos, bis);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                    bos.close();
                    bis.close();
                    outStream.close();
                    inStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 解析客户端的命令，得到请求的结果，并传输给客户端
     * @param url 访问的链接
     * @param bos 缓冲输出字节流
     * @param bis 缓冲输入字节流
     */
    public void getObject(String url, BufferedOutputStream bos, BufferedInputStream bis) throws IOException {
        int port;
        int strIndex;
        String serverName;
        String portToString;
        String filePath;
        //不断切片得到服务器地址，和请求文件名
        url = url.substring(7);
        strIndex = url.indexOf("/");
        serverName = url.substring(0, url.indexOf("/"));
        filePath = url.substring(url.indexOf("/"));

        if (serverName.contains(":")) {
            strIndex = serverName.indexOf(":");
            portToString = serverName.substring(strIndex + 1);
            serverName = serverName.substring(0, strIndex);
        } else {
            portToString = "80";
        }
        port = Integer.parseInt(portToString);

        Socket newSocket = new Socket(serverName, port);
        bis = new BufferedInputStream(newSocket.getInputStream());
        bos = new BufferedOutputStream(newSocket.getOutputStream());
        String request = "GET " + filePath + " HTTP/1.0" + CRLF + CRLF;
        byte[] buffer = new byte[1024];
        //向服务器传送命令
        bos.write(request.getBytes(), 0, request.length());
        bos.flush();
        int size = 0;
        while ((size = bis.read(buffer, 0, 1024)) != 0) {
            outStream.write(buffer, 0, 1024);//向客户端传输服务器发送的结果
        }
    }

}
