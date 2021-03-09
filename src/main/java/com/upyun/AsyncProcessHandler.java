package com.upyun;

import org.json.JSONArray;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import static com.upyun.UpYunUtils.md5;

public class AsyncProcessHandler {

    public static String HOST = "https://p0.api.upyun.com";
    protected final String AUTHORIZATION = "Authorization";
    protected final String DATE = "Date";

    // 空间名
    protected String bucketName = null;
    // 操作员名
    protected String userName = null;
    // 操作员密码
    protected String password = null;
    // 默认的超时时间：30秒
    protected int timeout = 30 * 1000;

    /**
     * 设置超时时间
     *
     * @param timeout 超时时间(ms)
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }


    /**
     * 发起异步处理请求
     *
     * @param params 请求参数
     * @return 请求结果
     * @throws IOException
     */
    protected Result process(Map<String, Object> params) throws IOException, UpException {

        params.put(CompressHandler.Params.TASKS, URLEncoder.encode(Base64Coder.encodeString(params.get(CompressHandler.Params.TASKS).toString()), "UTF-8"));

        InputStream is = null;
        OutputStream os;
        HttpURLConnection conn;

        URL url = new URL(HOST + "/pretreatment/");

        conn = (HttpURLConnection) url.openConnection();

        String date = UpYunUtils.getGMTDate();

        // 设置必要参数
        conn.setConnectTimeout(timeout);
        conn.setRequestMethod("POST");
        conn.setUseCaches(false);
        conn.setDoOutput(true);
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("User-Agent", UpYunUtils.VERSION);

        // 设置时间
        conn.setRequestProperty(DATE, date);
        // 设置签名
        conn.setRequestProperty(AUTHORIZATION,
                UpYunUtils.sign("POST", date, "/pretreatment/", userName, password, null));

        // 创建链接
        conn.connect();
        os = conn.getOutputStream();
        for (Map.Entry<String, Object> mapping : params.entrySet()) {
            os.write((mapping.getKey() + "=" + mapping.getValue().toString() + "&").getBytes("UTF-8"));
        }

        // 获取返回的信息
        Result result = getResp(conn);

        if (os != null) {
            os.close();
        }
        if (is != null) {
            is.close();
        }
        if (conn != null) {
            conn.disconnect();
        }
        return result;
    }


    /**
     * 初始化处理接口
     *
     * @param bucketName 空间名称
     * @param userName   操作员名称
     * @param password   密码，不需要MD5加密
     */
    public AsyncProcessHandler(String bucketName, String userName, String password) {
        this.bucketName = bucketName;
        this.userName = userName;
        this.password = md5(password);
    }

    protected Result getResp(HttpURLConnection conn) throws IOException {
        Result result = new Result();
        StringBuilder text = new StringBuilder();

        InputStream is = null;
        InputStreamReader sr = null;
        BufferedReader br = null;

        int code = conn.getResponseCode();

        result.setCode(code);

        try {
            is = code >= 400 ? conn.getErrorStream() : conn.getInputStream();

            sr = new InputStreamReader(is);
            br = new BufferedReader(sr);

            char[] chars = new char[4096];
            int length = 0;

            while ((length = br.read(chars)) != -1) {
                text.append(chars, 0, length);
            }
        } finally {
            if (br != null) {
                br.close();
                br = null;
            }
            if (sr != null) {
                sr.close();
                sr = null;
            }
            if (is != null) {
                is.close();
                is = null;
            }
        }
        if (code == HttpURLConnection.HTTP_OK) {
            result.setSucceed(true);
        } else {
            result.setSucceed(false);
        }
        result.setMsg(text.toString());
        return result;
    }

    /**
     * 解析返回数据,获取taskid
     *
     * @param raw 原始数据
     * @return taskid 数组
     */
    public String[] getTaskId(String raw) {
        JSONArray array = new JSONArray(raw);
        String ids[] = new String[array.length()];
        for (int i = 0; i < array.length(); i++) {
            ids[i] = (String) array.get(i);
        }
        return ids;
    }
}
