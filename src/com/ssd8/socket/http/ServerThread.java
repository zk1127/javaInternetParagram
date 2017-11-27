package com.ssd8.socket.http;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author:zengkang
 * @date:Create in 20:10 2017/11/26
 */
public class ServerThread {
    ServerSocket ss;
    final static int PORT = 80;
    final static String filebase = "./ServerFiles";
    ExecutorService executorService;
    int POOL_SIZE = 4;

    public ServerThread() throws IOException {
        ss = new ServerSocket(PORT);
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().
                availableProcessors() * POOL_SIZE);
        System.out.println("服务器启动.....");
    }

    /**
     * multithreading function
     * @throws IOException
     */
    public void service() throws IOException {
        Socket socket = null;
        while (true){
            socket = ss.accept();
            executorService.execute(new ServerHandler(socket));
        }
    }
    public static void main(String[] args) throws IOException {
        new ServerThread().service();;
    }
}
