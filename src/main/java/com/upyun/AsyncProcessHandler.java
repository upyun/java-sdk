package main.java.com.upyun;

import org.json.JSONArray;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

import static main.java.com.upyun.UpYunUtils.md5;

public class AsyncProcessHandler {

    public static String HOST = "http://p0.api.upyun.com";
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

        params.put(CompressHandler.Params.TASKS, Base64Coder.encodeString(params.get(CompressHandler.Params.TASKS).toString()));

        InputStream is = null;
        OutputStream os;
        HttpURLConnection conn;

        URL url = new URL(HOST + "/pretreatment/");

        conn = (HttpURLConnection) url.openConnection();

        String date = getGMTDate();

        // 设置必要参数
        conn.setConnectTimeout(timeout);
        conn.setRequestMethod("POST");
        conn.setUseCaches(false);
        conn.setDoOutput(true);
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("User-Agent", UpYunUtils.VERSION);
        conn.setRequestProperty("Date", date);

        // 设置时间
        conn.setRequestProperty(DATE, getGMTDate());
        // 设置签名
        conn.setRequestProperty(AUTHORIZATION,
                sign("POST", "/pretreatment/", date, params));

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
     * 获取签名算法
     *
     * @param params 参数组
     * @return 签名
     */
    protected String sign(String method, String URI, String date, Map<String, Object> params) throws UpException {

//        StringBuilder sb = new StringBuilder();
//        List<Map.Entry<String, Object>> list = new ArrayList<Map.Entry<String, Object>>(params.entrySet());
//        Collections.sort(list, new Comparator<Map.Entry<String, Object>>() {
//            public int compare(Map.Entry<String, Object> o1, Map.Entry<String, Object> o2) {
//                return o1.getKey().compareTo(o2.getKey());
//            }
//        });
//        for (Map.Entry<String, Object> mapping : list) {
//            sb.append(mapping.getKey() + mapping.getValue());
//        }
//
//        String sign = userName + sb.toString() + password;
//        return "UpYun " + userName + ":" + md5(sign);


//        String contentSt = null;
//
//        if (params != null) {
//            StringBuffer content = new StringBuffer();
//
//            for (Map.Entry<String, Object> mapping : params.entrySet()) {
//                content.append((mapping.getKey() + "=" + mapping.getValue().toString() + "&"));
//            }
//            contentSt = content.toString().trim();
//        }

        StringBuilder sb = new StringBuilder();
        String sp = "&";
        sb.append(method);
        sb.append(sp);
        sb.append(URI);

        sb.append(sp);
        sb.append(date);

//        if (contentSt != null && contentSt.length() > 0) {
//            sb.append(sp);
//            sb.append(UpYunUtils.md5(contentSt));
//        }
        String raw = sb.toString().trim();
        byte[] hmac = null;
        try {
            hmac = UpYunUtils.calculateRFC2104HMACRaw(password, raw);
        } catch (Exception e) {
            throw new UpException("calculate SHA1 wrong.");
        }

        if (hmac != null) {
            return "UpYun " + userName + ":" + Base64Coder.encodeLines(hmac).trim();
        }

        return null;


    }


    /**
     * 获取 GMT 格式时间戳
     *
     * @return GMT 格式时间戳
     */
    protected String getGMTDate() {
        SimpleDateFormat formater = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        formater.setTimeZone(TimeZone.getTimeZone("GMT"));
        return formater.format(new Date());
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
