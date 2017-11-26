package com.ssd8.socket.TCPandUDP.Multithread;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author:zengkang
 * @date:Create in 20:53 2017/11/14
 */
public class EchoClient {
    private static final int portTCP = 2021;//tcp端口号
    private static final int portUDP = 2020;//udp端口号
    private static final String host = "localhost";//主机名
    public static String ClientFileBase = "./ClientFiles/";//客户端文件根路径（用于存储接收文件）
    public static DatagramSocket dataSocket = null;
    public static byte[] receiveData;
    public static DatagramPacket dataPacket = null;
    public static Socket socket = null;

    public static void main(String[] args) throws IOException {
        try {
            socket = new Socket(host, portTCP);//建立tcp连接
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            String ret1 = dis.readUTF();//读取服务端信息
            System.err.println(ret1);
            Scanner input = new Scanner(System.in);//输入命令
            String cmd;
            while ((cmd = input.nextLine()) != null) {
                if (cmd.equals("")) {
                    cmd = input.nextLine();
                }
                dos.writeUTF(cmd);//向服务端发送命令
                if (cmd.equals("bye")) {//断开连接
//                        socket.close();
                    break;
//                        System.exit(0);
                }
                String ret2 = dis.readUTF();//读取服务端回复
                System.err.println(ret2);
                if (ret2.contains("进行文件传输")) {//进行udp接收
                    //正则表达式，通过服务端回复得到要接收文件的大小
                    Pattern pattern = Pattern.compile("\\d+");
                    Matcher matcher = pattern.matcher(ret2);
                    double fileLen = 0.0;
                    while (matcher.find()) {
                        fileLen = Double.parseDouble(matcher.group(0));//文件的大小
                        break;
                    }
                    if (cmd.contains("get")) {
                        get(cmd, fileLen);//接受文件
                    }
                }
            }
            dis.close();//关闭输入流
            dos.close();//关闭输出流
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @param cmd     命令
     * @param fileLen 文件长度
     * @throws Exception
     * @description 处理文件的接收
     */
    private static void get(String cmd, Double fileLen) throws Exception {
        System.err.println("开始接受文件");
        if (dataSocket == null)
            dataSocket = new DatagramSocket(portUDP);//建立端口号为portUDP的udp连接
        String[] split = cmd.split(" ");
        String fileName = split[1];
        //新建流进行处理接收的文件
        DataOutputStream fileOut = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(ClientFileBase + fileName)));
        receiveData = new byte[1024 * 4];//缓存数组
        int times = (int) (fileLen / receiveData.length);//循环接收次数
        int remain = (int) (fileLen % receiveData.length);//剩余不足1k的文件
        byte[] remainReceiveData = new byte[remain];
        for (int i = 0; i < times; i++) {//循环接收文件
            dataPacket = new DatagramPacket(receiveData, receiveData.length);
            dataSocket.receive(dataPacket);
            fileOut.write(receiveData, 0, dataPacket.getLength());//写入预定的文件
            fileOut.flush();
        }
        if (remain > 0) {
            //接收剩余不足1k的部分
            dataPacket = new DatagramPacket(remainReceiveData, remain);
            dataSocket.receive(dataPacket);
            fileOut.write(remainReceiveData, 0, dataPacket.getLength());//写入预定的文件
            fileOut.flush();
        }
        fileOut.close();
        dataSocket.close();
        System.err.println("接受完毕");
    }
}
