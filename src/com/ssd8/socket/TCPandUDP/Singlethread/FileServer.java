package com.ssd8.socket.TCPandUDP.Singlethread;

import java.io.*;
import java.net.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author:zengkang
 * @date:Create in 14:33 2017/11/11
 */
public class FileServer {
    private static final int portTCP = 2021;//tcp端口号
    private static final int portUDP = 2020;//udp端口号
    public static DatagramSocket dataSocket = null;
    public static DatagramPacket dataPacket;
    public static String FileBase = "./ServerFiles/";//服务器文件夹的根路径
    public static File folder = new File(FileBase);


    public static void main(String[] args) throws IOException {
        System.err.println("服务启动...");
        ServerSocket ss = new ServerSocket(portTCP);
        while (true) {
            Socket socket = null;
            try {
                socket = ss.accept();//tcp等候连接
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                dos.writeUTF(socket.getInetAddress() + ":" + socket.getPort() + ">连接成功");
                String readMsg = dis.readUTF();
                if (readMsg.equals("ls")) {//接收命令为ls
                    ls(dos);
                } else if (readMsg.contains("cd")) {//接收命令中存在cd
                    cd(readMsg,dos);
                } else if (readMsg.contains("get")) {//接收命令存在get
                    get(readMsg,dos);
                } else if (readMsg.equals("bye")) {//接收命令bye
                    dos.writeUTF("连接结束\n");
                    socket.close();
                    System.exit(0);
                } else {
                    dos.writeUTF("unknown cmd\n");//命令未知
                }
            } catch (IOException e) {
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
    }

    /**
     * @description 递归得到文件的大小
     * @param file 文件名
     * @return 文件的大小
     */
    private static double getDirSize(File file) {
        //判断文件是否存在
        if (file.exists()) {
            //如果是目录则递归计算其内容的总大小
            if (file.isDirectory()) {
                File[] children = file.listFiles();
                double size = 0;
                for (File f : children)
                    size += getDirSize(f);
                return size;
            } else {//如果是文件则直接返回其大小,以“K”为单位
                double size = (double) file.length() / 1024;
                return size;
            }
        } else {
            return 0.0;
        }
    }

    /**
     * @Description 处理命令为ls的方法
     * @param dos 输出字节流
     */
    private static void ls(DataOutputStream dos){
        File[] listOfFiles = folder.listFiles();//得到当前路径下的所有文件
        String str = "";
        double size;
        for (File listOfFile : listOfFiles) {
            size = getDirSize(listOfFile);//得到文件的大小
            if (listOfFile.isDirectory()) {
                str += "<dir>  " + listOfFile.getName() + "  " + size + "\n";
            } else if (listOfFile.isFile()) {
                size = listOfFile.length();
                str += "<File>  " + listOfFile.getName() + "  " + size + "\n";
            }
        }
        try {
            dos.writeUTF(str);//回复客户端
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @description 处理命令含有cd的方法
     * @param readMsg 传入的命令
     * @param dos 输出流
     * @throws IOException
     */
    private static void cd(String readMsg, DataOutputStream dos) throws IOException {
        String[] split = readMsg.split(" ");//切割字符串
        String newFolder="";
        if(split.length>1){
            newFolder = split[1];//得到要打开的文件名
        }else{
            dos.writeUTF("unknown cmd\n");
            return ;
        }
        int limit = 0;
        if (newFolder.equals("..")) { //cd .. 的处理
            Pattern pattern = Pattern.compile("/");//正则表达式处理字符串
            Matcher m = pattern.matcher(FileBase);
            while (m.find()) {
                limit++;//路径分割的份数
            }
            String[] split2 = FileBase.split("/", limit + 1);
            String cdFolder = "";
            int t;//截取路径的长度
            if (split2[limit].equals("")) {
                t = split2.length - 2;
            } else {
                t = split2.length - 1;
            }
            for (int i = 0; i < t; i++) {
                cdFolder = cdFolder + split2[i] + "/";//得到新路径
            }
            FileBase=cdFolder;
            folder = new File(FileBase);
            try {
                dos.writeUTF(cdFolder + "> OK\n");//回复客户端
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else { //cd xxx (xxx为文件名)
            Boolean flag=false;
            File[] listOfFiles = folder.listFiles();//得到当前路径的所有文件
            for (File listOfFile : listOfFiles) {
                if (listOfFile.getName().equals(newFolder)) {//判断是否存在该文件
                    flag=true;
                    FileBase = FileBase + newFolder+"/";
                    folder = new File(FileBase);//进入新得路径
                }
            }
            if(flag==true){
                try {
                    dos.writeUTF(newFolder + "> OK\n");//回复客户端
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                try {
                    dos.writeUTF("unknown dir\n");//如果不存在则回复unknown dir
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @description 处理命令含有get的方法
     * @param readMsg 命令
     * @param dos 输出流
     * @throws IOException
     */
    private static void get(String readMsg,DataOutputStream dos) throws IOException {
        final InetAddress host=InetAddress.getByName("localhost");
        String[] split = readMsg.split(" ");//分割字符串
        String fileName=null;
        if(split.length>1){
           fileName = split[1];//得到要获取的文件名
        }else{
            dos.writeUTF("unknown cmd\n");
            return ;
        }
        File[] listOfFiles = folder.listFiles();//得到当前路径的所有文件
        Boolean Flag = false;
        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                if (listOfFile.getName().equals(fileName)) {//判断是否存在该文件
                    Flag = true;
                }
            }
        }
        if (Flag == true) {//如果文件存在
            if (dataSocket == null) {
                dataSocket = new DatagramSocket();//建立udp
            }
            File sendFile = new File(FileBase + "/" + fileName);//要发送的文件
            int fileLen=(int) sendFile.length();
            byte[] sendData = new byte[1024];
            int times=fileLen/sendData.length;
            int remain=fileLen%sendData.length;
            byte[] remainData=new byte[remain];
            dos.writeUTF("进行文件传输,文件的大小为"+fileLen+",文件路径为"+FileBase);//回复客户端
            DataInputStream sendDis = new DataInputStream(new BufferedInputStream(
                    new FileInputStream(sendFile)));
            for (int i=0;i<times;i++){
                sendDis.read(sendData);
                dataPacket = new DatagramPacket(sendData, sendData.length,host, portUDP);
                dataSocket.send(dataPacket);
                try {
                    TimeUnit.MICROSECONDS.sleep(1);// 限制传输速度
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //传输剩余不足1k的部分
            sendDis.read(remainData);
            dataPacket = new DatagramPacket(remainData, remain,host, portUDP);
            dataSocket.send(dataPacket);
            try {
                TimeUnit.MICROSECONDS.sleep(1);// 限制传输速度
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            dataSocket.close();
        } else {//如果文件不存在
            try {
                dos.writeUTF("unknown file\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
