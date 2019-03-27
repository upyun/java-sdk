package com.upyun;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;


public class MediaHandler extends AsyncProcessHandler {


    /**
     * 初始化影视频处理接口
     *
     * @param bucketName 空间名称
     * @param userName   操作员名称
     * @param password   密码，不需要MD5加密
     */
    public MediaHandler(String bucketName, String userName, String password) {
        super(bucketName, userName, password);
    }


    /**
     * 发起异步音视频处理请求
     *
     * @param params 请求参数
     * @return 请求结果
     * @throws IOException
     */
    public Result process(Map<String, Object> params) throws IOException, UpException {
        return super.process(params);
    }


    /**
     * 发起查询处理进度请求
     *
     * @param params 请求参数
     * @return 请求结果
     * @throws IOException
     */
    public Result getStatus(Map<String, Object> params) throws IOException, UpException {
        InputStream is = null;
        HttpURLConnection conn;

        StringBuilder sb = new StringBuilder("/status");
        sb.append("?");
        for (Map.Entry<String, Object> mapping : params.entrySet()) {
            sb.append(mapping.getKey() + "=" + mapping.getValue().toString() + "&");
        }
        URL url = new URL(HOST + sb.toString().substring(0, sb.length() - 1));

        conn = (HttpURLConnection) url.openConnection();

        // 设置必要参数
        conn.setConnectTimeout(timeout);
        conn.setRequestMethod("GET");
        conn.setUseCaches(false);
        conn.setDoOutput(true);
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("User-Agent", UpYunUtils.VERSION);

        // 设置时间
        conn.setRequestProperty(DATE, getGMTDate());
        // 设置签名
        conn.setRequestProperty(AUTHORIZATION,
                sign("GET", sb.toString().substring(0, sb.length() - 1), getGMTDate(), null));

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
    public Result getResult(Map<String, Object> params) throws IOException, UpException {
        InputStream is = null;
        HttpURLConnection conn;

        StringBuilder sb = new StringBuilder("/result");
        sb.append("?");
        for (Map.Entry<String, Object> mapping : params.entrySet()) {
            sb.append(mapping.getKey() + "=" + mapping.getValue().toString() + "&");
        }
        URL url = new URL(HOST + sb.toString().substring(0, sb.length() - 1));

        conn = (HttpURLConnection) url.openConnection();

        // 设置必要参数
        conn.setConnectTimeout(timeout);
        conn.setRequestMethod("GET");
        conn.setUseCaches(false);
        conn.setDoOutput(true);
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("User-Agent", UpYunUtils.VERSION);

        // 设置时间
        conn.setRequestProperty(DATE, getGMTDate());
        // 设置签名
        conn.setRequestProperty(AUTHORIZATION,
                sign("GET", sb.toString().substring(0, sb.length() - 1), getGMTDate(), null));

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
