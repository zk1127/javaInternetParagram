package com.ssd8.socket.http;

/**
 * 构建文件类型的类
 * @Author:zengkang
 * @date:Create in 9:53 2017/12/1
 */
public class FileType {
    private String fileName;
    private String Type;

    /**
     * 构造函数
     * @param fileName 文件名
     */
    public FileType(String fileName) {
        this.fileName = fileName;
    }

    /**
     * 得到文件的类型
     * @param fileName 文件名
     * @return 文件的类型，返回字符串
     */
    public String getType(String fileName) {
        String postFix = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        if (postFix.equals("html") || postFix.equals("htm")) {
            return "text/html";
        } else if (postFix.equals("txt")) {
            return "text/plain";
        } else if (postFix.equals("jpg")) {
            return "image/jpg";
        } else if (postFix.equals("png")) {
            return "image/png";
        } else {
            return null;
        }
    }
}
