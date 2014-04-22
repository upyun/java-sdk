package com.upyun;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: zjzhai
 * Date: 4/1/14
 */
public class UpYunClient {

    private final String METHOD_HEAD = "HEAD";
    private final String METHOD_GET = "GET";
    private final String METHOD_PUT = "PUT";
    private final String METHOD_DELETE = "DELETE";


    /**
     * 是否创建文件夹
     */
    private final static String RECURSION_MKDIR_HEAD = "createFolder";
    /**
     * 认证
     */
    private final static String AUTHORIZATION = "Authorization";


    private final static String CONTENT_MD5_HEAD = "Content­MD5";

    private final static String DATE_HEAD = "Date";

    private final String CONTENT_LENGTH_HEAD = "Content-Length";

    private final static String CONTENT_SECRET_HEAD = "Content­Secret";

    private final String FILE_TYPE_HEAD = "x-upyun-file-type";

    private final String FILE_SIZE_HEAD = "x-upyun-file-size";

    private final String FILE_DATE_HEAD = "x-upyun-file-date";
    // 待上传文件的"访问密钥"
    private String fileSecret = null;
    /**
     * 路径的分割符
     */
    private final static String SEPARATOR = "/";


    // 默认的超时时间：30秒
    private int timeout = 30 * 1000;


    // 默认为自动识别接入点
    private String apiEntry = "v0.api.upyun.com";

    /**
     * 电信入口
     */
    public final static String TELECOMMUNICATIONS_ENTRY = "v1.api.upyun.com";

    /**
     * 联通网通入口
     */
    public final static String UNICOM_ENTRY = "v2.api.upyun.com";

    /**
     * 移动铁通入口
     */
    public final static String MOBILE_ENTRY = "v3.api.upyun.com";


    private String bucketName;
    private String userName;
    private String password;

    private boolean debug = true;

    private Map<String, String> params = new HashMap<String, String>();

    public UpYunClient(String bucketName, String userName, String password) {
        this.bucketName = bucketName;
        this.userName = userName;
        this.password = Crypto.md5(password);
    }

    public static UpYunClient create(String bucketName, String userName, String password) {
        return new UpYunClient(bucketName, userName, password);
    }

    /**
     * 重置所有参数
     */
    public void resetParams() {
        params.clear();
    }

    /**
     * 设置待上传文件的"访问密钥"
     * <p/>
     * 注意：<br>
     * 仅支持图片空！设置密钥后，无法根据原文件URL直接访问，需带 URL 后面加上 （缩略图间隔标志符+密钥） 进行访问
     * <p/>
     * 举例:<br>
     * 如果缩略图间隔标志符为"!"，密钥为"bac"，上传文件路径为"/folder/test.jpg"，<br>
     * 那么该图片的对外访问地址为：http://空间域名 /folder/test.jpg!bac
     *
     * @param secret 密钥字符串
     */
    public UpYunClient fileSecret(String secret) {
        this.fileSecret = secret;
        return this;
    }

    /**
     * 缩图的设置
     *
     * @param type  缩略图的类型
     * @param value 缩略类型对应的参数值，单位为像素,特别注意fix_width_or_height/fix_both：width x height(如 200x150)
     * @return
     */
    public UpYunClient picThumbnail(ThumbnailType type, Integer... value) {
        assert value != null && value.length > 0 && value.length <= 2;
        String realValue = "" + value[0];
        if (value.length > 1) {
            realValue += "x" + value[1];
        }
        params.put("x-gmkerl-type", type.getValue());
        params.put("x-gmkerl-value", realValue);
        return this;
    }

    /**
     * 设置缩略图的名称
     *
     * @param nameValue
     * @return
     */
    public UpYunClient picThumbnailName(String nameValue) {
        assert !_.isEmpty(nameValue);
        params.put("x-gmkerl-thumbnail", nameValue);
        return this;
    }

    /**
     * 图片压缩质量，范围1~100，默认 95。
     *
     * @param qualityValue
     * @return
     */
    public UpYunClient picThumbnailQuality(int qualityValue) {
        qualityValue = (qualityValue < 1) ? 1 : qualityValue;
        qualityValue = (qualityValue > 100) ? 100 : qualityValue;

        params.put("x-gmkerl-quality", "" + qualityValue);
        return this;
    }

    /**
     * 关闭锐化
     *
     * @return
     */
    public UpYunClient picThumbnailUnsharpen() {
        params.put("x-gmkerl-quality", "false");
        return this;
    }

    /**
     * 打开锐化
     *
     * @return
     */
    public UpYunClient picThumbnailSharpen() {
        params.put("x-gmkerl-quality", "true");
        return this;
    }

    /**
     * 自定义的缩略图版本名称，比如 small
     *
     * @return
     */
    public UpYunClient picThumbnailVersion(String versionValue) {
        assert !_.isEmpty(versionValue);
        params.put("x-gmkerl-thumbnail", versionValue);
        return this;
    }

    /**
     * 不保留原图的 EXIF 信息
     * 若原图带有 EXIF 信息并做缩略处理时，
     * 默认将删除 EXIF 信息
     *
     * @return
     */
    public UpYunClient unSaveExif() {
        params.put("x-gmkerl-exif-switch", "false");
        return this;
    }

    /**
     * 保留原图的 EXIF 信息
     * 若原图带有 EXIF 信息并做缩略处理时，
     * 默认将删除 EXIF 信息
     *
     * @return
     */
    public UpYunClient saveExif() {
        params.put("x-gmkerl-exif-switch", "true");
        return this;

    }

    /**
     * 图片旋转角度
     *
     * @param angle
     * @return
     */
    public UpYunClient picRotateAngle(PictureRotateAngle angle) {
        params.put("x-gmkerl-rotate", angle.getValue());
        return this;
    }

    /**
     * 图片裁剪 的相关参数的设置
     * <p/>
     * (x,y)：左上角坐标；
     * width：要裁剪的宽度；height：要裁剪的高度
     * x >= 0 && y >=0 && width > 0 && height > 0 且必须是正整型
     *
     * @param x
     * @param y
     * @param width
     * @param height
     * @return
     */
    public UpYunClient picCutCutting(int x, int y, int width, int height) {
        assert x >= 0 && y >= 0 && width > 0 && height > 0;
        params.put("x-gmkerl-crop", x + "," + y + "," + width + "," + height + "");
        return this;
    }

    /**
     * 设置连接超时时间，默认为30秒
     *
     * @param second 秒数，60即为一分钟超时
     */
    public UpYunClient timeout(int second) {
        timeout = second * 1000;
        return this;
    }


    /**
     * 选择电信接入点
     *
     * @return
     */
    public UpYunClient selectTelecomAPIEntry() {
        apiEntry = TELECOMMUNICATIONS_ENTRY;
        return this;
    }


    /**
     * 联通网通接入点
     *
     * @return
     */
    public UpYunClient selectUnicomAPIEntry() {
        apiEntry = UNICOM_ENTRY;
        return this;
    }

    /**
     * 移动铁通接入点
     *
     * @return
     */
    public UpYunClient selectMobileAPIEntry() {
        apiEntry = MOBILE_ENTRY;
        return this;
    }


    /**
     * 取消debug信息
     *
     * @return
     */
    public UpYunClient disableDebug() {
        debug = false;
        return this;
    }

    /**
     * 打开调试
     *
     * @return
     */
    public UpYunClient enableDebug() {
        debug = true;
        return this;
    }


    /**
     * 递归创建文件夹
     */
    public UpYunClient recursionMkDir() {
        params.put(RECURSION_MKDIR_HEAD, "true");
        return this;
    }


    /**
     * 不递归创建文件夹
     *
     * @return
     */
    public UpYunClient unRecursionMkDir() {
        params.put(RECURSION_MKDIR_HEAD, "false");
        return this;
    }

    /**
     * 设置文件的MD5传
     */
    public UpYunClient contentMD5(String md5Value) {
        params.put(CONTENT_MD5_HEAD, md5Value);
        return this;
    }


    /**
     * 上传文件用到的私钥
     */
    public UpYunClient contentSecret(String secretValue) {
        params.put(CONTENT_SECRET_HEAD, secretValue);
        return this;
    }

    /**
     * 文件的Content-type值
     */
    public UpYunClient contentType(String value) {
        params.put(CONTENT_SECRET_HEAD, value);
        return this;
    }


    /**
     * 得到文件信息
     *
     * @param path
     * @return
     */
    public FileItem getFileInfo(String path) {
        HttpURLConnection conn = null;

        try {
            // 获取链接
            conn = createDefaultConn(formatPath(path), METHOD_HEAD);

            // 设置签名
            conn.setRequestProperty(AUTHORIZATION, sign(conn, formatPath(path), 0));

            // 创建链接
            conn.connect();
            verifyConnectionCode(conn.getResponseCode(), conn.getResponseMessage());

            return new FileItem(
                    _.getFileNameFromPath(path),
                    conn.getHeaderField(FILE_TYPE_HEAD),
                    Long.valueOf(conn.getHeaderField(FILE_SIZE_HEAD)),
                    _.convertServerDateString(conn.getHeaderField(FILE_DATE_HEAD))
            );

        } catch (IOException e) {
            if (debug) e.printStackTrace();
            // 操作失败
            return null;

        } finally {
            _.close(conn);
        }
    }


    /**
     * 读取文件文本
     *
     * @param remoteFilePath
     * @return
     */
    public String readFileText(String remoteFilePath) {
        HttpURLConnection conn = null;
        try {
            // 获取链接
            conn = createDefaultConn(formatPath(remoteFilePath), METHOD_GET);

            // 设置签名
            conn.setRequestProperty(AUTHORIZATION, sign(conn, formatPath(remoteFilePath), 0));

            // 创建链接
            conn.connect();

            verifyConnectionCode(conn.getResponseCode(), conn.getResponseMessage());

            String result = _.readTextFromConnectionResponse(conn);
            conn.disconnect();
            conn = null;
            return result;

        } catch (IOException e) {
            if (debug)
                e.printStackTrace();

            // 操作失败
            return null;

        } finally {
            _.close(conn);
        }

    }

    /**
     * 获取总体空间的占用量
     *
     * @return 空间占用量，失败时返回 -1
     */
    public long getBucketUsage() {
        long usage = -1;

        String method = METHOD_GET;

        String url = formatPath("/") + "/?usage";

        HttpURLConnection conn = null;

        try {
            // 获取链接
            conn = createDefaultConn(url, method);
            // 设置签名
            conn.setRequestProperty(AUTHORIZATION, sign(conn, url, 0));

            // 创建链接
            conn.connect();

            String response = _.readTextFromConnectionResponse(conn);

            if (_.isEmpty(response)) throw new UpYunIOException("getBucketUsage failure");

            return Long.parseLong(response.trim());

        } catch (IOException e) {
            if (debug)
                e.printStackTrace();
            // 操作失败
            throw new UpYunIOException("getBucketUsage failure", e);
        } finally {
            _.closeConnAndStream(null, null, conn);
        }


    }

    /**
     * 列出路径下所有文件
     *
     * @param path
     * @return
     */
    public List<FileItem> listFiles(String path) {
        HttpURLConnection conn = null;
        try {
            // 获取链接
            conn = createDefaultConn(formatPath(path) + SEPARATOR, METHOD_GET);

            // 设置签名
            conn.setRequestProperty(AUTHORIZATION, sign(conn, formatPath(path) + SEPARATOR, 0));

            // 创建链接
            conn.connect();

            verifyConnectionCode(conn.getResponseCode(), conn.getResponseMessage());

            String result = _.readTextFromConnectionResponse(conn);
            if (_.isEmpty(result)) return null;
            return FileItem.convertFolderItems(result);

        } catch (IOException e) {
            if (debug)
                e.printStackTrace();

            // 操作失败
            return null;

        } finally {
            _.close(conn);
        }

    }


    /**
     * 下载文件
     *
     * @param path
     * @param downloadTo
     */
    public void downloadFile(String path, File downloadTo) {
        HttpURLConnection conn = null;
        OutputStream os = null;
        InputStream is = null;

        try {
            // 获取链接
            conn = createDefaultConn(formatPath(path), METHOD_GET);

            // 设置签名
            conn.setRequestProperty(AUTHORIZATION, sign(conn, formatPath(path), 0));

            wrapperParams(conn);

            // 创建链接
            conn.connect();

            os = new FileOutputStream(downloadTo);
            byte[] data = new byte[4096];
            int temp = 0;

            is = conn.getInputStream();

            while ((temp = is.read(data)) != -1) {
                os.write(data, 0, temp);
            }

            verifyConnectionCode(conn.getResponseCode(), conn.getResponseMessage());

        } catch (IOException e) {
            if (debug)
                e.printStackTrace();

            throw new UpYunBaseException(e);

        } finally {
            _.closeConnAndStream(os, is, conn);
        }
    }


    /**
     * 删除文件夹
     *
     * @param path
     */
    public void deleteFolder(String path) {
        HttpURLConnection conn = null;
        OutputStream os = null;
        InputStream is = null;

        try {
            // 获取链接
            conn = createDefaultConn(formatPath(path), METHOD_DELETE);
            // 设置签名
            conn.setRequestProperty(AUTHORIZATION, sign(conn, formatPath(path), 0));

            wrapperParams(conn);

            // 创建链接
            conn.connect();
            verifyConnectionCode(conn.getResponseCode(), conn.getResponseMessage());

        } catch (IOException e) {
            if (debug)
                e.printStackTrace();

            throw new UpYunIOException(e);

        } finally {
            _.closeConnAndStream(os, is, conn);
        }

    }


    /**
     * 删除文件
     *
     * @param filePath 文件路径（包含文件名）
     * @return true or false
     */
    public void deleteFile(String filePath) {
        HttpURLConnection conn = null;
        OutputStream os = null;
        InputStream is = null;
        try {
            // 获取链接
            conn = createDefaultConn(formatPath(filePath), METHOD_DELETE);

            // 设置签名
            conn.setRequestProperty(AUTHORIZATION, sign(conn, formatPath(filePath), 0));
            wrapperParams(conn);

            // 创建链接
            conn.connect();
            verifyConnectionCode(conn.getResponseCode(), conn.getResponseMessage());
        } catch (IOException e) {
            if (debug)
                e.printStackTrace();
            throw new UpYunIOException(e);
        } finally {
            _.closeConnAndStream(os, is, conn);

        }
    }


    /**
     * 创建文件夹
     *
     * @param path
     */
    public void createFolder(String path) {

        params.put("folder", "true");

        HttpURLConnection conn = null;
        OutputStream os = null;

        try {
            // 获取链接
            conn = createDefaultConn(formatPath(path), METHOD_PUT);

            // 设置签名
            conn.setRequestProperty(AUTHORIZATION, sign(conn, formatPath(path), 0));
            conn.setRequestProperty(CONTENT_LENGTH_HEAD, "0");

            wrapperParams(conn);

            // 创建链接
            conn.connect();

            os = conn.getOutputStream();
            os.flush();

            verifyConnectionCode(conn.getResponseCode(), conn.getResponseMessage());
        } catch (IOException e) {
            if (debug)
                e.printStackTrace();
            throw new UpYunBaseException(e);
        } finally {
            _.closeConnAndStream(os, null, conn);
        }
    }

    /**
     * 上传图片
     *
     * @param remotePath
     * @param upload
     * @return
     */
    public PictureItem uploadPicture(String remotePath, File upload) {
        assert upload != null;

        remotePath = formatPath(remotePath);

        InputStream is = null;
        OutputStream os = null;
        HttpURLConnection conn = null;

        try {
            // 读取待上传的文件
            is = new FileInputStream(upload);


            conn = createDefaultConn(remotePath, METHOD_PUT);

            // 设置签名
            conn.setRequestProperty(AUTHORIZATION, sign(conn, remotePath, is.available()));
            conn.setRequestProperty(CONTENT_LENGTH_HEAD, "" + is.available());

            // 是否自动创建父级目录
            conn.setRequestProperty(RECURSION_MKDIR_HEAD, "true");

            wrapperParams(conn);

            // 创建链接
            conn.connect();

            os = conn.getOutputStream();
            byte[] data = new byte[4096];
            int temp = 0;

            // 上传文件内容
            while ((temp = is.read(data)) != -1) {
                os.write(data, 0, temp);
            }


            verifyConnectionCode(conn.getResponseCode(), conn.getResponseMessage());


            resetParams();

            return new PictureItem(conn);

        } catch (IOException e) {
            if (debug)
                e.printStackTrace();

            throw new UpYunIOException(e);

        } finally {
            _.closeConnAndStream(os, is, conn);
        }
    }


    /**
     * 上传文件
     *
     * @param remotePath
     * @param upload
     */
    public void uploadFile(String remotePath, File upload) {
        assert upload != null;

        remotePath = formatPath(remotePath);

        InputStream is = null;
        OutputStream os = null;
        HttpURLConnection conn = null;

        try {
            // 读取待上传的文件
            is = new FileInputStream(upload);

            conn = createDefaultConn(remotePath, METHOD_PUT);

            // 设置签名
            conn.setRequestProperty(AUTHORIZATION, sign(conn, remotePath, is.available()));
            conn.setRequestProperty(CONTENT_LENGTH_HEAD, "" + is.available());

            // 是否自动创建父级目录
            conn.setRequestProperty(RECURSION_MKDIR_HEAD, "true");

            wrapperParams(conn);

            // 创建链接
            conn.connect();

            os = conn.getOutputStream();
            byte[] data = new byte[4096];
            int temp = 0;

            // 上传文件内容
            while ((temp = is.read(data)) != -1) {
                os.write(data, 0, temp);
            }

            verifyConnectionCode(conn.getResponseCode(), conn.getResponseMessage());

            resetParams();

        } catch (IOException e) {
            if (debug)
                e.printStackTrace();

            throw new UpYunIOException(e);

        } finally {
            _.closeConnAndStream(os, is, conn);
        }
    }


    /**
     * 上传文本
     *
     * @param remotePath
     * @param content
     */
    public void uploadFile(String remotePath, String content) {
        assert content != null;

        remotePath = formatPath(remotePath);

        OutputStream os = null;
        HttpURLConnection conn = null;


        try {

            byte[] data = content.getBytes("UTF-8");


            conn = createDefaultConn(remotePath, METHOD_PUT);
            wrapperParams(conn);
            // 设置签名
            conn.setRequestProperty(AUTHORIZATION, sign(conn, remotePath, data.length));
            conn.setRequestProperty(CONTENT_LENGTH_HEAD, "" + data.length);

            // 是否自动创建父级目录
            conn.setRequestProperty(RECURSION_MKDIR_HEAD, "true");


            // 创建链接
            conn.connect();

            os = conn.getOutputStream();
            int temp = 0;

            os = conn.getOutputStream();
            os.write(content.getBytes("UTF-8"));
            os.flush();

            verifyConnectionCode(conn.getResponseCode(), conn.getResponseMessage());

        } catch (IOException e) {
            if (debug)
                e.printStackTrace();

            throw new UpYunIOException(e);

        } finally {
            _.closeConnAndStream(os, null, conn);
        }
    }

    /**
     * 创建默认连接
     *
     * @param path
     * @param method
     * @return
     */
    private HttpURLConnection createDefaultConn(String path, String method) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL("http://" + apiEntry + path);
            conn = (HttpURLConnection) url.openConnection();
            // 设置必要参数
            conn.setConnectTimeout(timeout);
            conn.setUseCaches(false);
            conn.setDoOutput(true);
            conn.setRequestMethod(method);


            // 设置时间
            conn.setRequestProperty(DATE_HEAD, _.getGMTDate());
        } catch (MalformedURLException e) {
            throw new UpYunIOException("new URL of  " + path + " failure", e);

        } catch (IOException e) {
            throw new UpYunIOException("open " + path + " failure", e);
        }

        return conn;
    }


    /**
     * 校验HTTP的返回码
     *
     * @param code
     * @param responseMessage
     */
    private void verifyConnectionCode(int code, String responseMessage) {
        if (code == 400) {
            throw new UpYunBaseException("Bad Request");
        }

        if (code == 401 && "Unauthorized".equals(responseMessage))
            throw new UpYunAuthenticateException("not authenticated");

        if (code == 401 && "Sign error".equals(responseMessage))
            throw new UpYunBaseException("Sign error");

        if (code == 401 && "Need Date Header".equals(responseMessage))
            throw new UpYunBaseException("Need Date Header");

        /*权限错误(如非图片文件上传到图片空间)*/
        if (code == 403 && "Not Access".equals(responseMessage))
            throw new UpYunBaseException("Not Access");

        if (code == 403 && "File size too max".equals(responseMessage))
            throw new UpYunBaseException("File size too max");

        if (code == 403 && "Not a Picture File".equals(responseMessage))
            throw new UpYunBaseException("Not a Picture File");

        if (code == 403 && "Picture Size too max".equals(responseMessage))
            throw new UpYunBaseException("Picture Size too max");

        if (code == 403 && "Bucket full".equals(responseMessage))
            throw new UpYunBaseException("Bucket full");

        if (code == 403 && "Image Rotate Invalid Parameters".equals(responseMessage))
            throw new UpYunBaseException("Image Rotate Invalid Parameters");

        if (code == 403 && "Image Crop Invalid Parameters".equals(responseMessage))
            throw new UpYunBaseException("Image Crop Invalid Parameters");

        if (code == 404 && "Not Found".equals(responseMessage))
            throw new UpYunNotFoundException("Not Found");

        if (code == 406 && responseMessage.contains("Not Acceptable"))
            throw new UpYunBaseException("The folder exists");

        if (code >= 500)
            throw new UpYunServerErrorException("some errors at server");
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
                + conn.getRequestProperty(DATE_HEAD) + "&" + length + "&" + password;
        return "UpYun " + userName + ":" + Crypto.md5(sign);
    }

    /**
     * 格式化路径参数，去除前后的空格并确保以"/"开头，最后添加"/空间名"
     * <p/>
     * 最终构成的格式："/空间名/文件路径"
     *
     * @param path 目录路径或文件路径
     * @return 格式化后的路径
     */
    private String formatPath(String path) {
        if (!_.isEmpty(path)) {

            // 去除前后的空格
            path = path.trim();

            // 确保路径以"/"开头
            if (!path.startsWith(SEPARATOR)) {
                return SEPARATOR + bucketName + SEPARATOR + path;
            }
        }

        return SEPARATOR + bucketName + path;
    }

    /**
     * 向HttpURLConnect包装参数
     *
     * @param conn
     */
    private void wrapperParams(HttpURLConnection conn) {
        if (null == params || params.isEmpty()) return;

        for (Map.Entry<String, String> param : params.entrySet()) {
            conn.setRequestProperty(param.getKey(), param.getValue());
        }

    }


}
