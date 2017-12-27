package com.ssd8.socket.httpProxy;

import com.ssd8.socket.http.ServerHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author:zengkang
 * @date:Create in 20:10 2017/11/26
 */
public class ProxyThread {
    ServerSocket ss;
    final static int PORT = 8000;
    ExecutorService executorService;
    int POOL_SIZE = 4;

    /**
     * 初始化方法
     * @throws IOException
     */
    public ProxyThread() throws IOException {
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
            executorService.execute(new HttpProxy(socket));
        }
    }
    public static void main(String[] args) throws IOException {
        new ProxyThread().service();
    }
}
