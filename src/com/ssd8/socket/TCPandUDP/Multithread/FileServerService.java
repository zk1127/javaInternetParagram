package com.ssd8.socket.TCPandUDP.Multithread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @description 服务端的主函数
 * @Author:zengkang
 * @date:Create in 19:49 2017/11/14
 */
public class FileServerService {
    ServerSocket ss;
    private final int portTCP = 2021;//tcp端口号
    ExecutorService executorService;//线程池
    final int POOL_SIZE = 4;//单个处理器线程池的线程数目

    /**
     * @throws IOException
     * @description FileServerService的构造方法
     */
    public FileServerService() throws IOException {
        ss = new ServerSocket(portTCP);
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime()
                .availableProcessors() * POOL_SIZE);
        System.err.println("服务器启动....");
    }

    public static void main(String[] args) throws Exception {
        new FileServerService().servic();
    }

    /**
     * @throws IOException
     * @description 调用多线程的方法
     */
    public void servic() throws IOException {
        Socket socket = null;
        while (true) {
            try {
                socket = ss.accept(); // 等待用户连接
                executorService.execute(new Handler(socket)); // 把执行交给线程池来维护

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
