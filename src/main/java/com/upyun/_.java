package com.upyun;

import java.io.*;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 工具而已
 * User: zjzhai
 * Date: 4/1/14
 */
public class _ {

    /**
     * 路径的分割符
     */
    public final static String PATH_SEPARATOR = "/";

    /**
     * 将远程服务器传回的日期字符串转成日期类型
     *
     * @param serverResponse
     * @return
     */
    public static Date convertServerDateString(String serverResponse) {
        assert !isEmpty(serverResponse);
        return new Date(Long.valueOf(serverResponse) * 1000);
    }

    /**
     * 获取 GMT 格式时间戳
     *
     * @return GMT 格式时间戳
     */
    public static String getGMTDate() {
        SimpleDateFormat formater = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        formater.setTimeZone(TimeZone.getTimeZone("GMT"));
        return formater.format(new Date());
    }


    /**
     * 从路径中读取文件名
     *
     * @param path
     * @return
     */
    public static String getFileNameFromPath(String path) {
        assert !isEmpty(path);
        return path.substring(path.lastIndexOf(PATH_SEPARATOR) + 1, path.length());
    }

    public static boolean isEmpty(String str) {
        return null == str || "".equals(str.trim());

    }

    public static void close(InputStreamReader sr) {
        try {
            if (sr != null) {
                sr.close();
            }
            sr = null;
        } catch (IOException e) {
            throw new UpYunIOException(e);
        }
    }

    public static void close(BufferedReader br) {

        try {
            if (br != null) {
                br.close();

            }
            br = null;
        } catch (IOException e) {
            throw new UpYunIOException(e);
        }
    }

    /**
     * 读取HTTP请求的响应流
     *
     * @param conn
     * @return
     */
    public static String readTextFromConnectionResponse(HttpURLConnection conn) {
        StringBuilder text = new StringBuilder();
        InputStreamReader sr = null;
        BufferedReader br = null;
        InputStream is = null;
        int code = 0;
        try {
            code = conn.getResponseCode();
            is = (code >= 400) ? conn.getErrorStream() : conn.getInputStream();
            sr = new InputStreamReader(is);
            br = new BufferedReader(sr);
            char[] chars = new char[4096];
            int length = 0;
            while ((length = br.read(chars)) != -1) {
                text.append(chars, 0, length);
            }
        } catch (IOException e) {
            throw new UpYunIOException(e);
        } finally {
            try {
                if (sr != null) {
                    sr.close();
                    sr = null;
                }
                if (is != null) {
                    is.close();
                    is = null;
                }
                if (br != null) {
                    br.close();
                    br = null;
                }

            } catch (IOException e) {
                throw new UpYunIOException("", e);
            }
        }
        return text.toString();

    }


    /**
     * 关闭连接及流
     *
     * @param os
     * @param is
     * @param conn
     */
    public static void closeConnAndStream(OutputStream os, InputStream is, HttpURLConnection conn) {
        try {
            if (os != null) {
                os.close();
                os = null;
            }
            if (is != null) {
                is.close();
                is = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new UpYunIOException("closeConnAndStream OutputStream or InputStream failure", e);
        }

        if (conn != null) {
            conn.disconnect();
            conn = null;
        }
    }

    public static void close(HttpURLConnection conn) {
        if (conn != null) {
            conn.disconnect();
            conn = null;
        }
    }
}
