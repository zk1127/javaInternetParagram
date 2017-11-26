package com.ssd8.socket.TCPandUDP.Multithread;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author:zengkang
 * @date:Create in 20:52 2017/11/14
 */
public class Handler implements Runnable {
    private Socket socket;
    private final int portUDP = 2020;//udp端口号
    public DatagramSocket dataSocket = null;
    public DatagramPacket dataPacket;
    public String FileBase = "./ServerFiles/";//服务器文件夹的根路径
    public File folder = new File(FileBase);


    public Handler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            String prompt = socket.getInetAddress() + ":" + socket.getPort() + ">连接成功";
            dos.writeUTF(prompt);
            System.err.println(prompt);
            String readMsg = null;
            while ((readMsg = dis.readUTF()) != null) {
                if (readMsg.equals("ls")) {//接收命令为ls
                    ls(dos);
                } else if (readMsg.startsWith("cd")) {//接收命令中存在cd
                    cd(readMsg, dos);
                } else if (readMsg.startsWith("get")) {//接收命令存在get
                    get(readMsg, dos);
                } else if (readMsg.equals("bye")) {//接收命令bye
//                    dos.writeUTF("连接结束\n");
//                    socket.close();
                    break;
                } else {
                    dos.writeUTF("unknown cmd\n");//命令未知
                }
            }
            dis.close();
            dos.close();
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

    /**
     * @param file 文件名
     * @return 文件的大小
     * @description 递归得到文件的大小
     */
    private double getDirSize(File file) {
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
     * @param dos 输出字节流
     * @Description 处理命令为ls的方法
     */
    private void ls(DataOutputStream dos) {
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
     * @param readMsg 传入的命令
     * @param dos     输出流
     * @throws IOException
     * @description 处理命令含有cd的方法
     */
    private void cd(String readMsg, DataOutputStream dos) throws IOException {
        String[] split = readMsg.split(" ");//切割字符串
        String newFolder = "";
        if (split.length > 1) {
            newFolder = split[1];//得到要打开的文件名
        } else {
            dos.writeUTF("unknown cmd\n");
            return;
        }
        int limit = 0;
        if (newFolder.equals("..")) { //cd .. 的处理
            cdBack(dos, limit);
        } else { //cd xxx (xxx为文件名)
            cdFile(dos, newFolder);
        }
    }

    /**
     * @param dos   输出流
     * @param limit 处理文件名的字段数
     * @throws IOException
     * @description 处理cd ..的方法
     */
    private void cdBack(DataOutputStream dos, int limit) throws IOException {
        if (FileBase.equals("./ServerFiles/")) {
            dos.writeUTF(FileBase + ">OK+\n");
            return;
        }
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
        FileBase = cdFolder;
        folder = new File(FileBase);
        try {
            dos.writeUTF(cdFolder + "> OK\n");//回复客户端
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param dos       输出流
     * @param newFolder 打开的文件夹名
     * @description cd xx 打开xx文件夹的方法
     */
    private void cdFile(DataOutputStream dos, String newFolder) {
        Boolean flag = false;
        File[] listOfFiles = folder.listFiles();//得到当前路径的所有文件
        for (File listOfFile : listOfFiles) {
            if (listOfFile.getName().equals(newFolder) && listOfFile.isDirectory()) {//判断是否存在该文件
                flag = true;
                FileBase = FileBase + newFolder + "/";
                folder = new File(FileBase);//进入新得路径
            }
        }
        if (flag == true) {
            try {
                dos.writeUTF(newFolder + "> OK\n");//回复客户端
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                dos.writeUTF("unknown dir\n");//如果不存在则回复unknown dir
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * @param readMsg 命令
     * @param dos     输出流
     * @throws IOException
     * @description 处理命令含有get的方法
     */
    private void get(String readMsg, DataOutputStream dos) throws IOException {
        final InetAddress host = InetAddress.getByName("localhost");
        String[] split = readMsg.split(" ");//分割字符串
        String fileName = null;
        if (split.length > 1) {
            fileName = split[1];//得到要获取的文件名
        } else {
            dos.writeUTF("unknown cmd\n");
            return;
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
            int fileLen = (int) sendFile.length();
            byte[] sendData = new byte[1024 * 4];
            dos.writeUTF("进行文件传输,文件的大小为" + fileLen + ",文件路径为" + FileBase);//回复客户端
            DataInputStream sendDis = new DataInputStream(new BufferedInputStream(
                    new FileInputStream(sendFile)));
            int len = 0;
            while ((len = sendDis.read(sendData)) > 0) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                dataPacket = new DatagramPacket(sendData, len, host, portUDP);
                dataSocket.send(dataPacket);

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
