package main.java.com.upyun;

import org.json.JSONArray;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

import static main.java.com.upyun.UpYunUtils.md5;

public class MediaHandler {

    public static String HOST = "http://p0.api.upyun.com/";
    private final String AUTHORIZATION = "Authorization";
    private final String DATE = "Date";
    // 空间名
    protected String bucketName = null;
    // 操作员名
    protected String userName = null;
    // 操作员密码
    protected String password = null;
    // 默认的超时时间：30秒
    private int timeout = 30 * 1000;

    /**
     *  设置超时时间
     * @param timeout 超时时间(ms)
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }


    /**
     * 初始化影视频处理接口
     *
     * @param bucketName 空间名称
     * @param userName   操作员名称
     * @param password   密码，不需要MD5加密
     */
    public MediaHandler(String bucketName, String userName, String password) {
        this.bucketName = bucketName;
        this.userName = userName;
        this.password = md5(password);
    }


    /**
     * 发起异步音视频处理请求
     *
     * @param params 请求参数
     * @return 请求结果
     * @throws IOException
     */
    public Result process(Map<String, Object> params) throws IOException {

        params.put(Params.TASKS, Base64Coder.encodeString(params.get(Params.TASKS).toString()));

        InputStream is = null;
        OutputStream os;
        HttpURLConnection conn;

        URL url = new URL(HOST + "/pretreatment/");

        conn = (HttpURLConnection) url.openConnection();

        // 设置必要参数
        conn.setConnectTimeout(timeout);
        conn.setRequestMethod("POST");
        conn.setUseCaches(false);
        conn.setDoOutput(true);
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("User-Agent", "upyun-java-sdk/3.2");

        // 设置时间
        conn.setRequestProperty(DATE, getGMTDate());
        // 设置签名
        conn.setRequestProperty(AUTHORIZATION,
                sign(params));

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
     * 发起查询处理进度请求
     *
     * @param params 请求参数
     * @return 请求结果
     * @throws IOException
     */
    public Result getStatus(Map<String, Object> params) throws IOException {
        InputStream is = null;
        HttpURLConnection conn;

        StringBuilder sb = new StringBuilder(HOST + "status");
        sb.append("?");
        for (Map.Entry<String, Object> mapping : params.entrySet()) {
            sb.append(mapping.getKey() + "=" + mapping.getValue().toString() + "&");
        }
        URL url = new URL(sb.toString().substring(0, sb.length() - 1));

        conn = (HttpURLConnection) url.openConnection();

        // 设置必要参数
        conn.setConnectTimeout(timeout);
        conn.setRequestMethod("GET");
        conn.setUseCaches(false);
        conn.setDoOutput(true);
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("User-Agent", "upyun-java-sdk/3.2");

        // 设置时间
        conn.setRequestProperty(DATE, getGMTDate());
        // 设置签名
        conn.setRequestProperty(AUTHORIZATION,
                sign(params));

        // 创建链接
        conn.connect();

        // 获取返回的信息
        Result result = getResp(conn);

        if (is != null) {
            is.close();
        }
        if (conn != null) {
            conn.disconnect();
        }
        return result;
    }

    /**
     * 发起查询处理结果请求
     *
     * @param params 请求参数
     * @return 请求结果
     * @throws IOException
     */
    public Result getResult(Map<String, Object> params) throws IOException {
        InputStream is = null;
        HttpURLConnection conn;

        StringBuilder sb = new StringBuilder(HOST + "result");
        sb.append("?");
        for (Map.Entry<String, Object> mapping : params.entrySet()) {
            sb.append(mapping.getKey() + "=" + mapping.getValue().toString() + "&");
        }
        URL url = new URL(sb.toString().substring(0, sb.length() - 1));

        conn = (HttpURLConnection) url.openConnection();

        // 设置必要参数
        conn.setConnectTimeout(timeout);
        conn.setRequestMethod("GET");
        conn.setUseCaches(false);
        conn.setDoOutput(true);
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("User-Agent", "upyun-java-sdk/3.2");

        // 设置时间
        conn.setRequestProperty(DATE, getGMTDate());
        // 设置签名
        conn.setRequestProperty(AUTHORIZATION,
                sign(params));

        // 创建链接
        conn.connect();

        // 获取返回的信息
        Result result = getResp(conn);

        if (is != null) {
            is.close();
        }
        if (conn != null) {
            conn.disconnect();
        }
        return result;
    }

    private Result getResp(HttpURLConnection conn) throws IOException {
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
    public static String[] getTaskId(String raw) {
        JSONArray array = new JSONArray(raw);
        String ids[] = new String[array.length()];
        for (int i = 0; i < array.length(); i++) {
            ids[i] = (String) array.get(i);
        }
        return ids;
    }

    /**
     * 获取签名算法
     *
     * @param params 参数组
     * @return 签名
     */
    private String sign(Map<String, Object> params) {

        StringBuilder sb = new StringBuilder();
        List<Map.Entry<String, Object>> list = new ArrayList<Map.Entry<String, Object>>(params.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Object>>() {
            public int compare(Map.Entry<String, Object> o1, Map.Entry<String, Object> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });
        for (Map.Entry<String, Object> mapping : list) {
            sb.append(mapping.getKey() + mapping.getValue());
        }

        String sign = userName + sb.toString() + password;
        return "UpYun " + userName + ":" + md5(sign);
    }


    /**
     * 获取 GMT 格式时间戳
     *
     * @return GMT 格式时间戳
     */
    private String getGMTDate() {
        SimpleDateFormat formater = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        formater.setTimeZone(TimeZone.getTimeZone("GMT"));
        return formater.format(new Date());
    }

    public class Params {
        /**
         * 请求参数
         * <p>
         * bucket_name string  是	文件所在服务名称（空间名称）
         * notify_url	string	是	回调通知地址
         * source  string  是	待处理文件路径
         * tasks	string	是	处理任务信息，详见下
         * accept	string	是	必须指定为 json
         */
        public final static String BUCKET_NAME = "bucket_name";
        public final static String NOTIFY_URL = "notify_url";
        public final static String SOURCE = "source";
        public final static String TASKS = "tasks";
        public final static String ACCEPT = "accept";


        /**
         * 回调通知参数
         * <p>
         * status_code	integer	处理结果状态码，200 表示成功处理
         * path	array	输出文件保存路径
         * description	string	处理结果描述
         * task_id	string	任务对应的 task_id
         * info	string	视频文件的元数据信息。经过 base64 处理过之后的 JSON 字符串，仅当 type 为 video且 return_info 为 true 时返回
         * signature	string	回调验证签名，用户端程序可以通过校验签名，判断回调通知的合法性
         * timestamp	integer	服务器回调此信息时的时间戳
         */
        public final static String STATUS_CODE = "status_code";
        public final static String PATH = "path";
        public final static String DESCRIPTION = "description";
        public final static String TASK_ID = "task_id";
        public final static String INFO = "info";
        public final static String SIGNATURE = "signature";
        public final static String TIMESTAMP = "timestamp";

        /**
         * 查询参数
         * <p>
         * task_ids	string	任务 id 以 , 作为分隔符，最多 20 个
         */
        public final static String TASK_IDS = "task_ids";

        /**
         * 处理通用参数
         * <p>
         * type	string	音视频处理类型。不同的处理任务对应不同的 type，详见下方各处理任务说明
         * save_as	string	输出文件保存路径（同一个空间下），如果没有指定，系统自动生成在同空间同目录下
         * return_info	boolean	是否返回 JSON 格式元数据，默认 false。支持 type 值为 video 功能
         * avopts	string	音视频处理参数, 格式为 /key/value/key/value/...
         */
        public final static String TYPE = "type";
        public final static String SAVE_AS = "save_as";
        public final static String RETURN_INFO = "return_info";
        public final static String AVOPTS = "avopts";

    }
}
