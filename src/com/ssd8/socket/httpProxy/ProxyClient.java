package com.ssd8.socket.httpProxy;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * @Author:zengkang
 * @date:Create in 15:48 2017/12/9
 */
public class ProxyClient {
    private static final int port = 8000;
    private static final String host = "localhost";//主机名
    private static Socket socket;
    private static DataOutputStream dos = null;
    private static DataInputStream dis = null;

    public static void main(String[] args) throws Exception{
            try {
                socket = new Socket(host,port);
                dos = new DataOutputStream(socket.getOutputStream());
                dis = new DataInputStream(socket.getInputStream());
                socket = new Socket(host,port);
                Scanner input = new Scanner(System.in);//输入命令
                String cmd;
                while ((cmd = input.nextLine()) != null) {
                    dos.writeUTF(cmd);
                    String response = dis.readUTF();
                    System.out.println(response);
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                socket.close();
                dos.close();
                dis.close();
            }
    }
}
