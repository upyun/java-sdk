package com.upyun;

import okhttp3.*;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.upyun.RestManager.PARAMS.*;

public class RestManager {

    /**
     * 路径的分割符
     */
    private final String SEPARATOR = "/";

    private final String AUTHORIZATION = "Authorization";
    private final String DATE = "Date";
    private final String CONTENT_MD5 = "Content-MD5";


    private final String METHOD_HEAD = "HEAD";
    private final String METHOD_GET = "GET";
    private final String METHOD_PUT = "PUT";
    private final String METHOD_POST = "POST";
    private final String METHOD_DELETE = "DELETE";

    private OkHttpClient mClient;

    /**
     * 根据网络条件自动选择接入点:v0.api.upyun.com
     */
    public static final String ED_AUTO = "http://v0.api.upyun.com";
    /**
     * 电信接入点:v1.api.upyun.com
     */
    public static final String ED_TELECOM = "http://v1.api.upyun.com";
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
        params.put(KEY_X_UPYUN_COPY_SOURCE.value, sourcePath);
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
        params.put(KEY_X_UPYUN_MOVE_SOURCE.value, sourcePath);
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
        params.put(KEY_MAKE_DIR.value, "true");
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


        String sign = UpYunUtils.sign(method, date, uriPath, userName, password, params == null ? null : params.get(CONTENT_MD5));

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
         * 缩略图类型
         * <p>
         * 使用场景：上传图片时若无需保存原图，而只需某种特定大小的缩略图，比如说用户头像。
         * <p>
         * 说明：该参数必须搭配 KEY_X_GMKERL_VALUE 使用，否则无效。另外，使用该参数后将不保存原图，切忌。
         * <p>
         * 可选参数：<br>
         * 1)VALUE_FIX_MAX("fix_max")："限定最长边，短边自适应"<br>
         * 2)VALUE_FIX_MIN("fix_min")："限定最短边，长边自适应"<br>
         * 3)VALUE_FIX_WIDTH_OR_HEIGHT("fix_width_or_height")："限定宽度和高度"<br>
         * 4)VALUE_FIX_WIDTH("fix_width")："限定宽度，高度自适应"<br>
         * 5)VALUE_FIX_HEIGHT("fix_height")："限定高度，宽度自适应"<br>
         * 6)VALUE_FIX_BOTH("fix_both")："固定宽度和高度"<br>
         * 7)VALUE_FIX_SCALE("fix_scale")："等比例缩放"<br>
         * 8)VALUE_SQUARE("square")："方块图，固定高固定宽"<br>
         *
         * @see ：http://wiki.upyun.com/index.php?title=缩略图方式差别举例
         */
        KEY_X_GMKERL_TYPE("x-gmkerl-type"),

        /**
         * 缩略图参数值
         * <p>
         * 说明：该参数必须搭配 KEY_X_GMKERL_TYPE 使用，否则无效。具体的值需要根据 KEY_X_GMKERL_TYPE 而定。
         */
        KEY_X_GMKERL_VALUE("x-gmkerl-value"),

        /**
         * 缩略图质量：图片压缩质量，默认 95
         * <p>
         * 使用场景：用户上传高保真图片，但自身业务又无需太高质量的图片时，可以设置该参数减少文件保存的大小，从而减少空间的使用量。
         * <p>
         * 说明：使用该参数后将不保存原图，切忌。
         */
        KEY_X_GMKERL_QUALITY("x-gmkerl-quality"),

        /**
         * 图片锐化：默认锐化（true）
         * <p>
         * 使用场景：图片处理后质量太差，可以使用该参数模糊边缘，提高图片的清晰度或者焦距程度，使图片特定区域的色彩更加鲜明。
         * <p>
         * 说明：锐化不是万能的，很容易使图片不真实；另外，也无法通过锐化达到原图的效果。
         */
        KEY_X_GMKERL_UNSHARP("x-gmkerl-unsharp"),

        /**
         * 缩略图版本
         * <p>
         * 使用场景：快速处理原图，生成自定义的缩略图。
         * <p>
         * 说明：使用该参数前需要创建好缩略图版本号；另外，使用该参数后将不保存原图，切忌。
         *
         * @see :http://wiki.upyun.com/index.php?title=如何创建自定义缩略图
         */
        KEY_X_GMKERL_THUMBNAIL("x-gmkerl-thumbnail"),

        /**
         * 图片旋转
         * <p>
         * 使用场景：待上传的图片若是倾斜的，使用该参数可以直接进行强制的或自动的扶正。
         * <p>
         * 说明：只接受"auto"，"90"，"180"，"270"四种参数，其中"auto"参数根据图片 EXIF
         * 中的信息进行自动扶正，若图片没有 EXIF 信息，则该参数无效。另外，使用该参数后将不保存原图，切忌。
         *
         * @see :http://wiki.upyun.com/index.php?title=图片旋转
         */
        KEY_X_GMKERL_ROTATE("x-gmkerl-rotate"),

        /**
         * 图片裁剪
         * <p>
         * 使用场景：只需要保存待上传图片的某一个部分，比如用户上传头像图片进行裁剪。
         * <p>
         * 说明：参数格式为x,y,width,height，且需要满足 x >= 0 && y >=0 && width > 0 && height
         * > 0
         *
         * @see :http://wiki.upyun.com/index.php?title=图片裁剪
         */
        KEY_X_GMKERL_CROP("x-gmkerl-crop"),

        /**
         * 是否保留exif信息
         * <p>
         * 使用场景：对于原图包含EXIF信息，在上传图片时又进行了“破坏性处理”（比如裁剪、缩略、自定义版本等），
         * upyun默认会删除原图的EXIF信息。 此时搭配该参数可以保留原图的EXIF信息。比如旅游应用从缩略图中获取具体的地理信息。
         * <p>
         * 说明：仅搭配"破坏性处理"的参数使用时有效，其他处理均无效；另外key对应的值仅设置为"true"时有效；
         */
        KEY_X_GMKERL_EXIF_SWITCH("x-gmkerl-exif-switch"),

        /**
         * 创建目录
         * <p>
         * 说明：SDK内部使用
         */
        KEY_MAKE_DIR("folder"),

        /**
         * 复制文件
         * <p>
         * 说明：SDK内部使用
         */
        KEY_X_UPYUN_COPY_SOURCE("X-Upyun-Copy-Source"),

        /**
         * 移动文件
         * <p>
         * 说明：SDK内部使用
         */
        KEY_X_UPYUN_MOVE_SOURCE("X-Upyun-Move-Source"),

        /**
         * 分页参数
         * 分页开始位置，通过x-upyun-list-iter 响应头返回，所以第一次请求不需要填写
         */
        KEY_X_LIST_ITER("x-list-iter"),

        /**
         * 分页参数
         * 获取的文件数量，默认 100，最大 10000
         */
        KEY_X_LIST_LIMIT("x-list-limit"),

        /**
         * 分页参数
         * asc 或 desc，按文件名升序或降序排列。默认 asc
         */
        KEY_X_LIST_ORDER("x-list-order"),

        /**
         * 分页参数
         * application/json,返回json格式
         */
        KEY_ACCEPT("Accept"),

        /**
         * 删除参数
         * true 表示进行异步删除，不设置表示同步删除（默认）
         */
        KEY_X_UPYUN_ASYNC("x-upyun-async"),

        /**
         * 缩略图类型之 "限定最长边，短边自适应"，参数为像素值，如: 150
         */
        VALUE_FIX_MAX("fix_max"),
        /**
         * 缩略图类型之 "限定最短边，长边自适应"，参数为像素值，如: 150
         */
        VALUE_FIX_MIN("fix_min"),
        /**
         * 缩略图类型之 "限定宽度和高度"，参数为像素值，如: 150x130
         */
        VALUE_FIX_WIDTH_OR_HEIGHT("fix_width_or_height"),
        /**
         * 缩略图类型之 "限定宽度，高度自适应"，参数为像素值，如: 150
         */
        VALUE_FIX_WIDTH("fix_width"),
        /**
         * 缩略图类型之 "限定高度，宽度自适应"，参数为像素值，如: 150
         */
        VALUE_FIX_HEIGHT("fix_height"),
        /**
         * 缩略图类型之 "方块图，固定高固定宽"，参数为像素值，如: 150
         */
        VALUE_SQUARE("square"),
        /**
         * 缩略图类型之 "固定宽度和高度"，参数为像素值，如: 150x130
         */
        VALUE_FIX_BOTH("fix_both"),
        /**
         * 缩略图类型之 "等比例缩放"，参数为比例值（1-99），如: 50
         */
        VALUE_FIX_SCALE("fix_scale"),

        /**
         * 图片旋转之 "自动扶正"
         */
        VALUE_ROTATE_AUTO("auto"),
        /**
         * 图片旋转之 "旋转90度"
         */
        VALUE_ROTATE_90("90"),
        /**
         * 图片旋转之 "旋转180度"
         */
        VALUE_ROTATE_180("180"),
        /**
         * 图片旋转之 "旋转270度"
         */
        VALUE_ROTATE_270("270");

        private final String value;

        private PARAMS(String val) {
            value = val;
        }

        public String getValue() {
            return value;
        }
    }
}