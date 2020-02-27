package com.upyun;

import okhttp3.*;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class RestManager {

    /**
     * 路径的分割符
     */
    private final String SEPARATOR = "/";

    private final String AUTHORIZATION = "Authorization";
    private final String DATE = "Date";


    private final String METHOD_HEAD = "HEAD";
    private final String METHOD_GET = "GET";
    private final String METHOD_PUT = "PUT";
    private final String METHOD_POST = "POST";
    private final String METHOD_DELETE = "DELETE";

    private OkHttpClient mClient;

    /**
     * 根据网络条件自动选择接入点:v0.api.upyun.com
     */
    public static final String ED_AUTO = "https://v0.api.upyun.com";
    /**
     * 电信接入点:v1.api.upyun.com
     */
    public static final String ED_TELECOM = "https://v1.api.upyun.com";
    /**
     * 联通网通接入点:v2.api.upyun.com
     */
    public static final String ED_CNC = "https:/v2.api.upyun.com";
    /**
     * 移动铁通接入点:v3.api.upyun.com
     */
    public static final String ED_CTT = "https://v3.api.upyun.com";

    // 默认不开启debug模式
    public boolean debug = false;
    // 默认的超时时间：30秒
    private int timeout = 30;
    // 默认为自动识别接入点
    private String apiDomain = ED_AUTO;
    // 待上传文件的 Content-MD5 值
    private String contentMD5 = null;
    // 待上传文件的"访问密钥"
    private String fileSecret = null;
    // 空间名
    protected String bucketName = null;
    // 操作员名
    protected String userName = null;
    // 操作员密码
    protected String password = null;

    /**
     * 初始化 UpYun 存储接口
     *
     * @param bucketName 空间名称
     * @param userName   操作员名称
     * @param password   密码，不需要MD5加密
     * @return UpYun object
     */
    public RestManager(String bucketName, String userName, String password) {
        this.bucketName = bucketName;
        this.userName = userName;
        this.password = UpYunUtils.md5(password);
        this.mClient = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 切换 API 接口的域名接入点
     * <p>
     * 可选参数：<br>
     * 1) UpYun.ED_AUTO(v0.api.upyun.com)：默认，根据网络条件自动选择接入点 <br>
     * 2) UpYun.ED_TELECOM(v1.api.upyun.com)：电信接入点<br>
     * 3) UpYun.ED_CNC(v2.api.upyun.com)：联通网通接入点<br>
     * 4) UpYun.ED_CTT(v3.api.upyun.com)：移动铁通接入点
     *
     * @param domain 域名接入点
     */
    public void setApiDomain(String domain) {
        this.apiDomain = domain;
    }

    /**
     * 查看当前的域名接入点
     *
     * @return
     */
    public String getApiDomain() {
        return apiDomain;
    }

    /**
     * 设置连接超时时间，默认为30秒
     *
     * @param second 秒数，60即为一分钟超时
     */
    public void setTimeout(int second) {
        this.timeout = second;
        this.mClient = mClient.newBuilder()
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 设置代理
     */
    public void setProxy(Proxy proxy) {
        this.mClient = mClient.newBuilder()
                .proxy(proxy)
                .build();
    }

    /**
     * 查看当前的超时时间
     *
     * @return
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * 获取当前SDK的版本号
     *
     * @return SDK版本号
     */
    public String version() {
        return UpYunUtils.VERSION;
    }


    /**
     * 上传文件
     *
     * @param filePath 文件路径（包含文件名）
     * @param data     文件内容
     * @return response
     */
    public Response writeFile(String filePath, byte[] data, Map<String, String> params) throws IOException, UpException {
        return request(METHOD_PUT, filePath, RequestBody.create(null, data), params);
    }

    /**
     * 上传文件
     *
     * @param filePath 文件路径（包含文件名）
     * @param file     待上传的文件
     * @param params   额外参数
     * @return response
     * @throws IOException
     */
    public Response writeFile(String filePath, File file, Map<String, String> params) throws IOException, UpException {
        return request(METHOD_PUT, filePath, RequestBody.create(null, file), params);
    }

    /**
     * 上传文件
     *
     * @param filePath    文件路径（包含文件名）
     * @param inputStream 待上传的 inputStream
     * @param params      额外参数
     * @return response
     * @throws IOException
     */
    public Response writeFile(String filePath, InputStream inputStream, Map<String, String> params) throws IOException, UpException {
        return request(METHOD_PUT, filePath, create(null, inputStream), params);
    }

    /**
     * 复制文件
     *
     * @param path       目标路径
     * @param sourcePath 原路径
     * @return
     * @throws IOException
     * @throws UpException
     */
    public Response copyFile(String path, String sourcePath, Map<String, String> params) throws IOException, UpException {
        if (params == null) {
            params = new HashMap<String, String>();
        }
        params.put(PARAMS.X_UPYUN_COPY_SOURCE.getValue(), sourcePath);
        return request(METHOD_PUT, path, RequestBody.create(null, ""), params);
    }

    /**
     * 复制文件
     *
     * @param path       目标路径
     * @param sourcePath 原路径
     * @return
     * @throws IOException
     * @throws UpException
     */
    public Response moveFile(String path, String sourcePath, Map<String, String> params) throws IOException, UpException {
        if (params == null) {
            params = new HashMap<String, String>();
        }
        params.put(PARAMS.X_UPYUN_COPY_SOURCE.getValue(), sourcePath);
        return request(METHOD_PUT, path, RequestBody.create(null, ""), params);
    }


    /**
     * 读取文件
     *
     * @param filePath 文件路径（包含文件名）
     * @return 文件内容 或 null
     */
    public Response readFile(String filePath) throws IOException, UpException {
        return request(METHOD_GET, filePath, null, null);
    }


    /**
     * 删除文件
     *
     * @param filePath 文件路径（包含文件名）
     * @param params   额外参数
     * @return response
     */
    public Response deleteFile(String filePath, Map<String, String> params) throws IOException, UpException {
        return request(METHOD_DELETE, filePath, null, params);
    }

    /**
     * 创建目录
     *
     * @param filePath 目录路径
     * @return response
     */
    public Response mkDir(String filePath) throws IOException, UpException {
        Map<String, String> params = new HashMap<String, String>();
        params.put(PARAMS.MAKE_DIR.getValue(), "true");
        return request(METHOD_POST, filePath, RequestBody.create(null, ""), params);
    }

    /**
     * 删除目录
     *
     * @param filePath 目录路径
     * @return response
     */
    public Response rmDir(String filePath) throws IOException, UpException {
        return request(METHOD_DELETE, filePath, null, null);
    }

    /**
     * 获取文件信息
     *
     * @param filePath 文件路径（包含文件名）
     * @return 文件信息 或 null
     */
    public Response getFileInfo(String filePath) throws IOException, UpException {
        return request(METHOD_HEAD, filePath, null, null);
    }


    /**
     * 读取目录列表
     *
     * @param path   目录路径
     * @param params 分页参数
     * @return response
     */
    public Response readDirIter(String path, Map<String, String> params) throws IOException, UpException {
        return request(METHOD_GET, path, null, params);
    }

    /**
     * 获取服务使用量
     *
     * @return response
     */
    public Response getBucketUsage() throws IOException, UpException {
        return request(METHOD_GET, "/?usage", null, null);
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

    private RequestBody create(final MediaType mediaType, final InputStream inputStream) {
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return mediaType;
            }

            @Override
            public long contentLength() {
                try {
                    return inputStream.available();
                } catch (IOException e) {
                    return 0;
                }
            }

            public void writeTo(BufferedSink bufferedSink) throws IOException {
                Source source = null;
                try {
                    source = Okio.source(inputStream);
                    bufferedSink.writeAll(source);
                } finally {
                    Util.closeQuietly(source);
                }
            }
        };
    }


    private Response request(String method, String filePath, RequestBody body, Map<String, String> params) throws UpException, IOException {

        String date = getGMTDate();

        String uriPath = SEPARATOR + bucketName + filePath;


        String sign = UpYunUtils.sign(method, date, uriPath, userName, password, params == null ? null : params.get(PARAMS.CONTENT_MD5.getValue()));

        // 获取链接
        String url = apiDomain + uriPath;

        Request.Builder builder = new Request.Builder()
                .url(url)
                .header(DATE, date)
                .header(AUTHORIZATION, sign)
                .header("User-Agent", UpYunUtils.VERSION)
                .method(method, body);


        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.header(entry.getKey(), entry.getValue());
            }
        }
        return mClient.newCall(builder.build()).execute();
    }

    /**
     * 其他额外参数的键值和参数值
     */
    public enum PARAMS {

        /**
         * 创建目录
         * <p>
         * 说明：SDK内部使用
         */
        MAKE_DIR("folder"),

        /**
         * 复制文件
         * <p>
         * 说明：SDK内部使用
         */
        X_UPYUN_COPY_SOURCE("X-Upyun-Copy-Source"),

        /**
         * 移动文件
         * <p>
         * 说明：SDK内部使用
         */
        X_UPYUN_MOVE_SOURCE("X-Upyun-Move-Source"),

        /**
         * 分页参数
         * 分页开始位置，通过x-upyun-list-iter 响应头返回，所以第一次请求不需要填写
         */
        X_LIST_ITER("x-list-iter"),

        /**
         * 分页参数
         * 获取的文件数量，默认 100，最大 10000
         */
        X_LIST_LIMIT("x-list-limit"),

        /**
         * 分页参数
         * asc 或 desc，按文件名升序或降序排列。默认 asc
         */
        X_LIST_ORDER("x-list-order"),

        /**
         * 分页参数
         * application/json,返回json格式
         */
        ACCEPT("Accept"),

        /**
         * 删除参数
         * true 表示进行异步删除，不设置表示同步删除（默认）
         */
        X_UPYUN_ASYNC("x-upyun-async"),

        /**
         * 上传参数
         * 上传文件的 MD5 值，如果请求中文件太大计算 MD5 不方便，可以为空
         */
        CONTENT_MD5("Content-MD5"),
        /**
         * 上传参数
         * 文件类型，默认使用文件扩展名作为文件类型
         */
        CONTENT_TYPE("Content-Type"),
        /**
         * 文件密钥，用于保护文件，防止文件被直接访问
         */
        CONTENT_SECRET("Content-Secret"),
        /**
         * 文件元信息
         */
        X_UPYUN_META_X("x-upyun-meta-x"),
        /**
         * 文件元信息, 指定文件的生存时间，单位天
         */
        X_UPYUN_META_TTL("x-upyun-meta-ttl"),
        /**
         * 图片预处理参数
         */
        X_GMKERL_THUMB("x-gmkerl-thumb"),
        /**
         * 处理源文件的元信息
         */
        X_UPYUN_METADATA_DIRECTIVE("X-Upyun-Metadata-Directive"),
        /**
         * 获取文件信息参数
         */
        X_UPYUN_FILE_TYPE("x-upyun-file-type"),
        X_UPYUN_FILE_SIZE("x-upyun-file-size"),
        X_UPYUN_FILE_DATE("x-upyun-file-date");

        private final String value;

        private PARAMS(String val) {
            value = val;
        }

        public String getValue() {
            return value;
        }
    }
}