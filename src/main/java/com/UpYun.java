package com;

import com.upyun.UpAPIException;
import com.upyun.UpException;
import com.upyun.UpYunUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Deprecated
public class UpYun {

    /**
     * 默认的编码格式
     */
    private static final String UTF8 = "UTF-8";


    /**
     * 路径的分割符
     */
    private final String SEPARATOR = "/";

    private final String AUTHORIZATION = "Authorization";
    private final String DATE = "Date";
    private final String CONTENT_LENGTH = "Content-Length";
    private final String CONTENT_MD5 = "Content-MD5";
    private final String CONTENT_SECRET = "Content-Secret";
    private final String MKDIR = "mkdir";


    private final String METHOD_HEAD = "HEAD";
    private final String METHOD_GET = "GET";
    private final String METHOD_PUT = "PUT";
    private final String METHOD_DELETE = "DELETE";

    /**
     * 根据网络条件自动选择接入点:v0.api.upyun.com
     */
    public static final String ED_AUTO = "v0.api.upyun.com";
    /**
     * 电信接入点:v1.api.upyun.com
     */
    public static final String ED_TELECOM = "v1.api.upyun.com";
    /**
     * 联通网通接入点:v2.api.upyun.com
     */
    public static final String ED_CNC = "v2.api.upyun.com";
    /**
     * 移动铁通接入点:v3.api.upyun.com
     */
    public static final String ED_CTT = "v3.api.upyun.com";

    // 默认不开启debug模式
    public boolean debug = false;
    // 默认的超时时间：30秒
    private int timeout = 30 * 1000;
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

    //接口返回 header
    private Map<String, String> headers;

    /**
     * 初始化 UpYun 存储接口
     *
     * @param bucketName 空间名称
     * @param userName   操作员名称
     * @param password   密码，不需要MD5加密
     * @return UpYun object
     */
    public UpYun(String bucketName, String userName, String password) {
        this.bucketName = bucketName;
        this.userName = userName;
        this.password = md5(password);
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
        this.timeout = second * 1000;
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
     * 查看当前是否是debug模式
     *
     * @return
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * 设置是否开启debug模式
     *
     * @param debug
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * 设置待上传文件的 Content-MD5 值
     * <p>
     * 如果又拍云服务端收到的文件MD5值与用户设置的不一致，将回报 406 Not Acceptable 错误
     *
     * @param md5Value 文件 MD5 校验后的内容
     */
    public void setContentMD5(String md5Value) {
        this.contentMD5 = md5Value;
    }

    /**
     * 设置待上传文件的"访问密钥"
     * <p>
     * 注意：<br>
     * 仅支持图片空！设置密钥后，无法根据原文件URL直接访问，需带 URL 后面加上 （缩略图间隔标志符+密钥） 进行访问
     * <p>
     * 举例:<br>
     * 如果缩略图间隔标志符为"!"，密钥为"bac"，上传文件路径为"/folder/test.jpg"，<br>
     * 那么该图片的对外访问地址为：http://空间域名 /folder/test.jpg!bac
     *
     * @param secret 密钥字符串
     */
    public void setFileSecret(String secret) {
        this.fileSecret = secret;
    }

//    public String getPicWidth() {
//        return picWidth;
//    }
//
//    public String getPicHeight() {
//        return picHeight;
//    }
//
//    public String getPicFrames() {
//        return picFrames;
//    }
//
//    public String getPicType() {
//        return picType;
//    }

    /**
     * 获取当前SDK的版本号
     *
     * @return SDK版本号
     */
    public String version() {
        return UpYunUtils.VERSION;
    }

    /**
     * 获取总体空间的占用量
     *
     * @return 空间占用量，失败时返回 -1
     */
    public long getBucketUsage() throws IOException, UpException {
        long usage = -1;

        String result = HttpAction(METHOD_GET, formatPath("/") + "/?usage");

        if (!isEmpty(result)) {

            try {
                usage = Long.parseLong(result.trim());
            } catch (NumberFormatException e) {
            }
        }

        return usage;
    }

    /**
     * 获取某个子目录的占用量
     *
     * @param path 目标路径
     * @return 空间占用量，失败时返回 -1
     */
    @Deprecated
    public long getFolderUsage(String path) throws IOException, UpException {

        long usage = -1;

        String result = HttpAction(METHOD_GET, formatPath(path) + "/?usage");

        if (!isEmpty(result)) {

            try {
                usage = Long.parseLong(result.trim());
            } catch (NumberFormatException e) {
            }
        }

        return usage;
    }

    /**
     * 上传文件
     *
     * @param filePath 文件路径（包含文件名）
     * @param datas    文件内容
     * @return true or false
     */
    public boolean writeFile(String filePath, byte[] datas) throws IOException, UpException {
        return writeFile(filePath, datas, false, null);
    }

    /**
     * 上传文件
     *
     * @param filePath 文件路径（包含文件名）
     * @param datas    文件内容
     * @param auto     是否自动创建父级目录(最多10级)
     * @return true or false
     */
    public boolean writeFile(String filePath, byte[] datas, boolean auto) throws IOException, UpException {
        return writeFile(filePath, datas, auto, null);
    }

    /**
     * 上传文件
     *
     * @param filePath 文件路径（包含文件名）
     * @param datas    文件内容
     * @param auto     是否自动创建父级目录(最多10级)
     * @param params   额外参数
     * @return true or false
     */
    public boolean writeFile(String filePath, byte[] datas, boolean auto,
                             Map<String, String> params) throws IOException, UpException {

        return HttpAction(METHOD_PUT, formatPath(filePath), datas, null, auto,
                params) != null;
    }

    /**
     * 上传文件
     *
     * @param filePath 文件路径（包含文件名）
     * @param datas    datas 文件内容
     * @return true or false
     */
    public boolean writeFile(String filePath, String datas) throws IOException, UpException {
        return writeFile(filePath, datas, false, null);
    }

    /**
     * 上传文件
     *
     * @param filePath 文件路径（包含文件名）
     * @param datas    datas 文件内容
     * @param auto     是否自动创建父级目录(最多10级)
     * @return true or false
     */
    public boolean writeFile(String filePath, String datas, boolean auto) throws IOException, UpException {
        return writeFile(filePath, datas, auto, null);
    }

    /**
     * 上传文件
     *
     * @param filePath 文件路径（包含文件名）
     * @param datas    datas 文件内容
     * @param auto     是否自动创建父级目录(最多10级)
     * @param params   额外参数
     * @return true or false
     */
    public boolean writeFile(String filePath, String datas, boolean auto,
                             Map<String, String> params) throws IOException, UpException {

        return writeFile(filePath, datas.getBytes(UTF8), auto, params);
    }

    /**
     * 上传文件
     *
     * @param filePath 文件路径（包含文件名）
     * @param file     待上传的文件
     * @return true or false
     * @throws IOException
     */
    public boolean writeFile(String filePath, File file) throws IOException, UpException {
        return writeFile(filePath, file, false, null);
    }

    /**
     * 上传文件
     *
     * @param filePath 文件路径（包含文件名）
     * @param file     待上传的文件
     * @param auto     是否自动创建父级目录(最多10级)
     * @return true or false
     * @throws IOException
     */
    public boolean writeFile(String filePath, File file, boolean auto)
            throws IOException, UpException {
        return writeFile(filePath, file, auto, null);
    }

    /**
     * 上传文件
     *
     * @param filePath 文件路径（包含文件名）
     * @param file     待上传的文件
     * @param auto     是否自动创建父级目录(最多10级)
     * @param params   额外参数
     * @return true or false
     * @throws IOException
     */
    public boolean writeFile(String filePath, File file, boolean auto,
                             Map<String, String> params) throws IOException, UpException {

        InputStream inputStream = new FileInputStream(file);
        return writeFile(filePath, inputStream, auto, params);
    }

    /**
     * 上传文件
     *
     * @param filePath    文件路径（包含文件名）
     * @param inputStream 待上传的 inputStream
     * @param auto        是否自动创建父级目录(最多10级)
     * @param params      额外参数
     * @return true or false
     * @throws IOException
     */
    public boolean writeFile(String filePath, InputStream inputStream, boolean auto,
                             Map<String, String> params) throws IOException, UpException {
        filePath = formatPath(filePath);

        OutputStream os = null;
        HttpURLConnection conn = null;

        // 获取链接
        URL url = new URL("https://" + apiDomain + filePath);
        conn = (HttpURLConnection) url.openConnection();

        // 设置必要参数
        conn.setConnectTimeout(timeout);
        conn.setRequestMethod(METHOD_PUT);
        conn.setUseCaches(false);
        conn.setDoOutput(true);
        conn.setChunkedStreamingMode(0);

        String date = UpYunUtils.getGMTDate();

        // 设置时间
        conn.setRequestProperty(DATE, date);
        conn.setRequestProperty("User-Agent", UpYunUtils.VERSION);

        // 设置文件的 MD5 参数
        if (!isEmpty(this.contentMD5)) {
            conn.setRequestProperty(CONTENT_MD5, this.contentMD5);
        }

        // 设置签名
//            conn.setRequestProperty(AUTHORIZATION,
//                    sign(conn, filePath, file.length()));

        conn.setRequestProperty(AUTHORIZATION,
                UpYunUtils.sign(METHOD_PUT, date, filePath, userName, password, contentMD5).trim());
        this.contentMD5 = null;

        // 设置文件的访问密钥
        if (!isEmpty(this.fileSecret)) {
            conn.setRequestProperty(CONTENT_SECRET, this.fileSecret);
            this.fileSecret = null;
        }

        // 是否自动创建父级目录
        if (auto) {
            conn.setRequestProperty(MKDIR, "true");
        }

        // 设置额外的参数，如图片缩略图等
        if (params != null && !params.isEmpty()) {

            for (Map.Entry<String, String> param : params.entrySet()) {
                conn.setRequestProperty(param.getKey(), param.getValue());
            }
        }

        // 创建链接
        conn.connect();

        os = conn.getOutputStream();
        byte[] data = new byte[4096];
        int temp = 0;

        // 上传文件内容
        while ((temp = inputStream.read(data)) != -1) {
            os.write(data, 0, temp);
        }
        // 获取返回的信息
        getText(conn, false);

        if (os != null) {
            os.close();
            os = null;
        }
        if (inputStream != null) {
            inputStream.close();
            inputStream = null;
        }
        if (conn != null) {
            conn.disconnect();
            conn = null;
        }
        // 上传成功
        return true;
    }

    /**
     * 读取文件
     *
     * @param filePath 文件路径（包含文件名）
     * @return 文件内容 或 null
     */
    public String readFile(String filePath) throws IOException, UpException {
        return HttpAction(METHOD_GET, formatPath(filePath));
    }

    /**
     * 读取文件
     *
     * @param filePath 文件路径（包含文件名）
     * @param file     临时文件
     * @return true or false
     */
    public boolean readFile(String filePath, File file) throws IOException, UpException {

        String result = HttpAction(METHOD_GET, formatPath(filePath), null,
                file, false);

        return "".equals(result);
    }

    /**
     * 获取文件信息
     *
     * @param filePath 文件路径（包含文件名）
     * @return 文件信息 或 null
     */
    public Map<String, String> getFileInfo(String filePath) throws IOException, UpException {

        HttpAction(METHOD_HEAD, formatPath(filePath));

        return headers;
    }

    /**
     * 删除文件
     *
     * @param filePath 文件路径（包含文件名）
     * @param params   额外参数
     * @return true or false
     */
    public boolean deleteFile(String filePath, Map<String, String> params) throws IOException, UpException {

        return HttpAction(METHOD_DELETE, formatPath(filePath), params) != null;
    }

    /**
     * 创建目录
     *
     * @param path 目录路径
     * @return true or false
     */
    public boolean mkDir(String path) throws IOException, UpException {
        return mkDir(path, false);
    }

    /**
     * 创建目录
     *
     * @param path 目录路径
     * @param auto 是否自动创建父级目录(最多10级)
     * @return true or false
     */
    public boolean mkDir(String path, boolean auto) throws IOException, UpException {

        Map<String, String> params = new HashMap<String, String>(1);
        params.put(PARAMS.KEY_MAKE_DIR.getValue(), "true");

        String result = HttpAction(METHOD_PUT, formatPath(path), null, null,
                auto, params);

        return result != null;
    }

    /**
     * 读取目录列表
     *
     * @param path   目录路径
     * @param params 分页参数
     * @return List<FolderItem> 或 null
     */
    @Deprecated
    public List<FolderItem> readDir(String path, Map<String, String> params) throws IOException, UpException {

        String result = HttpAction(METHOD_GET, formatPath(path) + SEPARATOR, params);

        if (isEmpty(result))
            return null;

        List<FolderItem> list = new LinkedList<FolderItem>();

        String[] datas = result.split("\n");

        for (int i = 0; i < datas.length; i++) {
            if (datas[i].indexOf("\t") > 0) {
                list.add(new FolderItem(datas[i]));
            }
        }
        return list;
    }

    /**
     * 读取目录列表
     *
     * @param path   目录路径
     * @param params 分页参数
     * @return StringJson 或 null
     */
    public String readDirJson(String path, Map<String, String> params) throws IOException, UpException {

        if (params == null) {
            params = new HashMap<String, String>();
        }

        params.put("Accept", "application/json");

        return HttpAction(METHOD_GET, formatPath(path) + SEPARATOR, params);

    }

    /**
     * 读取目录列表
     *
     * @param path   目录路径
     * @param params 分页参数
     * @return FolderItemIter 或 null
     */
    public FolderItemIter readDirIter(String path, Map<String, String> params) throws IOException, UpException {

        if (params == null) {
            params = new HashMap<String, String>();
        }

        params.put("Accept", "application/json");

        String result = HttpAction(METHOD_GET, formatPath(path) + SEPARATOR, params);

        JSONObject jObject1 = new JSONObject(result);
        FolderItemIter folderItemIter = new FolderItemIter();
        folderItemIter.iter = jObject1.getString("iter");
        folderItemIter.files = new ArrayList<FolderItem>();
        JSONArray jsonArray2 = jObject1.getJSONArray("files");
        for (int i = 0; i < jsonArray2.length(); i++) {
            JSONObject jObject3 = jsonArray2.getJSONObject(i);
            FolderItem folderItem = new FolderItem(jObject3.getString("type"), jObject3.getString("name"), jObject3.getLong("length"), jObject3.getLong("last_modified"));
            folderItemIter.files.add(folderItem);
        }

        return folderItemIter;
    }

    /**
     * 删除目录
     *
     * @param path 目录路径
     * @return true or false
     */
    public boolean rmDir(String path) throws IOException, UpException {
        return HttpAction(METHOD_DELETE, formatPath(path)) != null;
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
    public boolean copyFile(String path, String sourcePath) throws IOException, UpException {


        Map<String, String> params = new HashMap<String, String>(1);
        params.put(PARAMS.KEY_X_UPYUN_COPY_SOURCE.getValue(), sourcePath);

        String result = HttpAction(METHOD_PUT, formatPath(path), null, null,
                false, params);

        return result != null;
    }

    /**
     * 移动文件
     *
     * @param path       目标路径
     * @param sourcePath 原路径
     * @return
     * @throws IOException
     * @throws UpException
     */
    public boolean moveFile(String path, String sourcePath) throws IOException, UpException {

        Map<String, String> params = new HashMap<String, String>(1);
        params.put(PARAMS.KEY_X_UPYUN_MOVE_SOURCE.getValue(), sourcePath);

        String result = HttpAction(METHOD_PUT, formatPath(path), null, null,
                false, params);

        return result != null;
    }


    /**
     * 获取接口返回 header 信息
     *
     * @return
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * 对字符串进行 MD5 加密
     *
     * @param str 待加密字符串
     * @return 加密后字符串
     */
    public static String md5(String str) {
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f'};
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            md5.update(str.getBytes(UTF8));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        byte[] encodedValue = md5.digest();
        int j = encodedValue.length;
        char finalValue[] = new char[j * 2];
        int k = 0;
        for (int i = 0; i < j; i++) {
            byte encoded = encodedValue[i];
            finalValue[k++] = hexDigits[encoded >> 4 & 0xf];
            finalValue[k++] = hexDigits[encoded & 0xf];
        }

        return new String(finalValue);
    }

    /**
     * 对文件进行 MD5 加密
     *
     * @param file 待加密的文件
     * @return 文件加密后的 MD5 值
     * @throws IOException
     */
    public static String md5(File file) throws IOException {
        FileInputStream is = new FileInputStream(file);
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f'};
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            int n = 0;
            byte[] buffer = new byte[1024];
            do {
                n = is.read(buffer);
                if (n > 0) {
                    md5.update(buffer, 0, n);
                }
            } while (n != -1);
            is.skip(0);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        } finally {
            is.close();
        }

        byte[] encodedValue = md5.digest();

        int j = encodedValue.length;
        char finalValue[] = new char[j * 2];
        int k = 0;
        for (int i = 0; i < j; i++) {
            byte encoded = encodedValue[i];
            finalValue[k++] = hexDigits[encoded >> 4 & 0xf];
            finalValue[k++] = hexDigits[encoded & 0xf];
        }

        return new String(finalValue);
    }

    /**
     * 计算签名
     *
     * @param conn   连接
     * @param uri    请求地址
     * @param length 请求所发Body数据长度
     * @return 签名字符串
     */
    private String sign(HttpURLConnection conn, String uri, long length) {
        String sign = conn.getRequestMethod() + "&" + uri + "&"
                + conn.getRequestProperty(DATE) + "&" + length + "&" + password;
        return "UpYun " + userName + ":" + md5(sign);
    }

    /**
     * 连接处理逻辑
     *
     * @param method 请求方式 {GET, POST, PUT, DELETE}
     * @param uri    请求地址
     * @return 请求结果（字符串）或 null
     */
    private String HttpAction(String method, String uri) throws IOException, UpException {
        return HttpAction(method, uri, null, null, false);
    }

    /**
     * 连接处理逻辑
     *
     * @param method 请求方式 {GET, POST, PUT, DELETE}
     * @param uri    请求地址
     * @param param  请求参数
     * @return 请求结果（字符串）或 null
     */
    private String HttpAction(String method, String uri, Map<String, String> param) throws IOException, UpException {
        return HttpAction(method, uri, null, null, false, param);
    }

    /**
     * 连接处理逻辑
     *
     * @param method  请求方式 {GET, POST, PUT, DELETE}
     * @param uri     请求地址
     * @param datas   该请求所需发送数据（可为 null）
     * @param outFile 文件描述符（可为 null）
     * @param auto    自动创建父级目录(最多10级)
     * @return 请求结果（字符串）或 null
     */
    private String HttpAction(String method, String uri, byte[] datas,
                              File outFile, boolean auto) throws IOException, UpException {

        return HttpAction(method, uri, datas, outFile, auto, null);
    }

    /**
     * 连接处理逻辑
     *
     * @param method  请求方式 {GET, POST, PUT, DELETE}
     * @param uri     请求地址
     * @param datas   该请求所需发送数据（可为 null）
     * @param outFile 文件描述符（可为 null）
     * @param auto    自动创建父级目录(最多10级)
     * @param params  额外参数
     * @return 请求结果（字符串）或 null
     */
    private String HttpAction(String method, String uri, byte[] datas,
                              File outFile, boolean auto, Map<String, String> params) throws IOException, UpException {

        String result = null;

        HttpURLConnection conn = null;
        OutputStream os = null;
        InputStream is = null;

        // 获取链接
        URL url = new URL("https://" + apiDomain + uri);
        conn = (HttpURLConnection) url.openConnection();

        // 设置必要参数
        conn.setConnectTimeout(timeout);
        conn.setRequestMethod(method);
        conn.setUseCaches(false);
        if (!method.equals(METHOD_DELETE) && !method.equals(METHOD_HEAD) && !method.equals(METHOD_GET)) {
            conn.setDoOutput(true);
            conn.setChunkedStreamingMode(0);
        }

        String date = UpYunUtils.getGMTDate();

        // 设置时间
        conn.setRequestProperty(DATE, date);
        conn.setRequestProperty("User-Agent", UpYunUtils.VERSION);

        // 是否自动创建父级目录
        if (auto) {
            conn.setRequestProperty(MKDIR, "true");
        }

        long contentLength = 0;

        if (datas == null) {
            conn.setRequestProperty(CONTENT_LENGTH, "0");
        } else {
            contentLength = datas.length;
            conn.setRequestProperty(CONTENT_LENGTH,
                    String.valueOf(datas.length));

            // 设置文件的 MD5 参数
            if (!isEmpty(this.contentMD5)) {
                conn.setRequestProperty(CONTENT_MD5, this.contentMD5);
            }
            // 设置文件的访问密钥
            if (!isEmpty(this.fileSecret)) {
                conn.setRequestProperty(CONTENT_SECRET, this.fileSecret);
                this.fileSecret = null;
            }
        }

        // 设置签名
//            conn.setRequestProperty(AUTHORIZATION,
//                    sign(conn, uri, contentLength));

        conn.setRequestProperty(AUTHORIZATION,
                UpYunUtils.sign(method, date, uri, userName, password, contentMD5));
        this.contentMD5 = null;
        // 是否是创建文件目录
        boolean isFolder = false;

        // 设置额外的参数，如图片缩略图等
        if (params != null && !params.isEmpty()) {

            isFolder = !isEmpty(params.get(PARAMS.KEY_MAKE_DIR.getValue()));

            for (Map.Entry<String, String> param : params.entrySet()) {
                conn.setRequestProperty(param.getKey(), param.getValue());
            }
        }

        // 创建链接
        conn.connect();

        if (datas != null) {
            os = conn.getOutputStream();
            os.write(datas);
            os.flush();
        }

        if (isFolder) {
            os = conn.getOutputStream();
            os.flush();
        }

        if (outFile == null) {

            result = getText(conn, METHOD_HEAD.equals(method));

        } else {
            result = "";

            os = new FileOutputStream(outFile);
            byte[] data = new byte[4096];
            int temp = 0;

            is = conn.getInputStream();

            while ((temp = is.read(data)) != -1) {
                os.write(data, 0, temp);
            }
        }
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
        }

        if (conn != null) {
            conn.disconnect();
            conn = null;
        }
        return result;
    }

    /**
     * 获得连接请求的返回数据
     *
     * @param conn
     * @return 字符串
     */
    private String getText(HttpURLConnection conn, boolean isHeadMethod)
            throws IOException, UpAPIException {

        StringBuilder text = new StringBuilder();

        InputStream is = null;
        InputStreamReader sr = null;
        BufferedReader br = null;
        headers = null;

        int code = conn.getResponseCode();
        try {
//            is = conn.getInputStream();
            is = code >= 400 ? conn.getErrorStream() : conn.getInputStream();

            if (!isHeadMethod) {
                sr = new InputStreamReader(is);
                br = new BufferedReader(sr);

                char[] chars = new char[4096];
                int length = 0;

                while ((length = br.read(chars)) != -1) {
                    text.append(chars, 0, length);
                }
            }
            if (code >= 200 && code < 300) {
                headers = new HashMap<String, String>();
                Map h = conn.getHeaderFields();
                Set<String> keys = h.keySet();
                for (String key : keys) {
                    String val = conn.getHeaderField(key);
                    if (key != null && val != null) {
                        headers.put(key, val);
                    }
                }
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

        if (isHeadMethod) {
            if (code >= 400)
                return null;
            return "";
        }

        if (code >= 400)
            throw new UpAPIException(code, text.toString());

        return text.toString();
    }

    /**
     * 判断字符串是否为空
     * getTextgetText
     *
     * @param str
     * @return 是否为空
     */
    private boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * 格式化路径参数，去除前后的空格并确保以"/"开头，最后添加"/空间名"
     * <p>
     * 最终构成的格式："/空间名/文件路径"
     *
     * @param path 目录路径或文件路径
     * @return 格式化后的路径
     */
    private String formatPath(String path) {

        if (!isEmpty(path)) {

            // 去除前后的空格
            path = path.trim();

            // 确保路径以"/"开头
            if (!path.startsWith(SEPARATOR)) {
                return SEPARATOR + bucketName + SEPARATOR + path;
            }
        }

        return SEPARATOR + bucketName + path;
    }

    public class FolderItem {
        // 文件名
        public String name;

        // 文件类型 {file, folder}
        public String type;

        // 文件大小
        public long size;

        // 文件日期
        public Date date;


        public FolderItem(String data) {
            String[] a = data.split("\t");
            if (a.length == 4) {
                this.name = a[0];
                this.type = ("N".equals(a[1]) ? "File" : "Folder");
                try {
                    this.size = Long.parseLong(a[2].trim());
                } catch (NumberFormatException e) {
                    this.size = -1;
                }
                long da = 0;
                try {
                    da = Long.parseLong(a[3].trim());
                } catch (NumberFormatException e) {
                }
                this.date = new Date(da * 1000);
            }
        }

        public FolderItem(String type, String name, long length, long last_modified) {
            this.type = type;
            this.name = name;
            this.size = length;
            this.date = new Date(last_modified * 1000);
        }

        @Override
        public String toString() {
            return "FolderItem{" +
                    "name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    ", size=" + size +
                    ", date=" + date +
                    '}';
        }

    }

    public class FolderItemIter {

        public String iter;

        public ArrayList<FolderItem> files;

        @Override
        public String toString() {
            return "FolderItemIter{" +
                    "iter='" + iter + '\'' +
                    ", files=" + files +
                    '}';
        }
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
