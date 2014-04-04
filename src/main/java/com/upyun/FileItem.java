package com.upyun;

import java.net.HttpURLConnection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * User: zjzhai
 * Date: 3/31/14
 */
public class FileItem {

    public static final String X_UPYUN_WIDTH = "x-upyun-width";
    public static final String X_UPYUN_HEIGHT = "x-upyun-height";
    public static final String X_UPYUN_FRAMES = "x-upyun-frames";



    // 文件名
    private String name;

    // 文件类型 {file, folder}
    private String type;

    // 文件大小
    private long size;

    // 文件日期
    private Date date;

    public FileItem(String httpConnResponseData) {
        String[] a = httpConnResponseData.split("\t");
        if (a.length == 4) {
            this.name = a[0];
            this.type = ("N".equals(a[1]) ? "File" : "Folder");
            try {
                this.size = Long.parseLong(a[2].trim());
            } catch (NumberFormatException e) {
                this.size = -1;
            }
            this.date = _.convertServerDateString(a[3].trim());
        }
    }

    public FileItem(String name, String type, long size, Date date) {
        this.name = name;
        this.type = type;
        this.size = size;
        this.date = date;

    }

    public static List<FileItem> convertFolderItems(String data) {
        List<FileItem> list = new LinkedList<FileItem>();

        String[] items = data.split("\n");

        for (int i = 0; i < items.length; i++) {
            if (items[i].indexOf("\t") > 0) {
                list.add(new FileItem(items[i]));
            }
        }
        return list;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public long getSize() {
        return size;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "time = " + date + "  size = " + size + "  type = " + type
                + "  name = " + name;
    }
}
