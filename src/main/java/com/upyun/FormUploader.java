package com.upyun;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Map;

public class FormUploader {

    // 默认的超时时间：30秒
    private int timeout = 30 * 1000;

    //默认过期时间
    private int expiration = 1800;

    //默认域名
    private String apiDomain = "http://v0.api.upyun.com";
    // 空间名
    protected String bucketName = null;
    // 操作员名
    protected String userName = null;
    // 操作员密码
    protected String password = null;

    public int getTimeout() {
        return timeout;
    }

    public String getBucketName() {
        return bucketName;
    }

    public int getExpiration() {
        return expiration;
    }

    public void setExpiration(int expiration) {
        this.expiration = expiration;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public FormUploader(String bucketName, String userName, String password) {
        this.bucketName = bucketName;
        this.userName = userName;
        this.password = UpYunUtils.md5(password);
    }

    /**
     * 表单上传方法
     *
     * @param params 参数组见官网API文档
     * @param file   上传文件
     * @return 返回结果
     */
    public Result upload(Map<String, Object> params, File file) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {

        return upload(params, file, null);
    }

    /**
     * 表单上传方法
     *
     * @param params 参数组见官网API文档
     * @param datas  上传数组
     * @return 返回结果
     */
    public Result upload(Map<String, Object> params, byte[] datas) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {

        return upload(params, null, datas);
    }

    private Result upload(Map<String, Object> params, File file, byte[] datas) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        if (params.get(Params.BUCKET) == null) {
            params.put(Params.BUCKET, this.bucketName);
        }

        if (params.get(Params.EXPIRATION) == null) {
            params.put(Params.EXPIRATION, System.currentTimeMillis() / 1000 + expiration);
        }

        String policy = UpYunUtils.getPolicy(params);

        String signature = null;


        String date = (String) params.get(Params.DATE);
        String contentMd5 = (String) params.get(Params.CONTENT_MD5);

        StringBuilder sb = new StringBuilder();
        String sp = "&";
        sb.append("POST");
        sb.append(sp);
        sb.append("/" + bucketName);

        if (date != null) {
            sb.append(sp);
            sb.append(date);
        }

        sb.append(sp);
        sb.append(policy);

        if (contentMd5 != null) {
            sb.append(sp);
            sb.append(contentMd5);
        }

        String raw = sb.toString().trim();

        byte[] hmac;

        hmac = UpYunUtils.calculateRFC2104HMACRaw(password, raw);

        if (hmac != null) {
            signature = Base64Coder.encodeLines(hmac);
        }

        System.out.println("sign" + signature);

        URL url = null;
        try {
            url = new URL(getApiDomain() + "/" + this.bucketName);
        } catch (MalformedURLException e) {
            throw new RuntimeException("域名错误格式错误");
        }

        Result result;
        try {
            result = postData(file, datas, url, policy, signature.trim());
        } catch (IOException e) {
            result = new Result();
            result.setSucceed(false);
            result.setMsg(e.toString());
        }

        return result;
    }

    private Result postData(File file, byte[] datas, URL url, String policy, String signature) throws IOException {

        String BOUNDARY = "---------------------------12121upyun";

        InputStream is = null;
        OutputStream os;
        HttpURLConnection conn;

        conn = (HttpURLConnection) url.openConnection();

        // 设置必要参数
        conn.setConnectTimeout(timeout);
        conn.setRequestMethod("POST");
        conn.setUseCaches(false);
        conn.setDoOutput(true);
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("User-Agent", UpYunUtils.VERSION);
        conn.setRequestProperty("x-upyun-api-version", "2");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
        conn.setChunkedStreamingMode(0);

        // 创建链接
        conn.connect();

        os = conn.getOutputStream();
        os.write(getBoundaryStr("policy", BOUNDARY, policy).getBytes());
//        os.write(getBoundaryStr("signature", BOUNDARY, signature).getBytes());
        os.write(getBoundaryStr("authorization", BOUNDARY, "UPYUN " + userName + ":" + signature).getBytes());


        String fileName = file != null ? file.getName() : "null";
        os.write(getBoundaryFileStr("file", BOUNDARY, fileName).getBytes());

        byte[] tempData = new byte[4096];
        int temp = 0;
        if (file != null) {
            // 上传文件内容
            is = new FileInputStream(file);
            while ((temp = is.read(tempData)) != -1) {
                os.write(tempData, 0, temp);
            }
        } else if (datas != null) {
            os.write(datas);
        }

        os.write("\r\n".getBytes());
        os.write(paramsEnd(BOUNDARY).getBytes());

        // 获取返回的信息
        Result result = getResult(conn);

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

    private String getBoundaryStr(String key, String BOUNDARY, String value) {
        StringBuffer strBuf = new StringBuffer();
        strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
        strBuf.append("Content-Disposition: form-data; name=\"" + key + "\"\r\n\r\n");
        strBuf.append(value);
        return strBuf.toString();
    }

    private String getBoundaryFileStr(String key, String BOUNDARY, String filename) {
        StringBuffer strBuf = new StringBuffer();
        strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
        strBuf.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + filename + "\"\r\n\r\n");
//        strBuf.append("Content-Type:" + filename + "\r\n\r\n");
        return strBuf.toString();
    }

    private Result getResult(HttpURLConnection conn) throws IOException {
        Result result = new Result();
        StringBuilder text = new StringBuilder();

        InputStream is = null;
        InputStreamReader sr = null;
        BufferedReader br = null;

        int code = conn.getResponseCode();

        result.setCode(code);

        try {
//            is = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
            is = conn.getInputStream();

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

    //添加结尾数据
    private String paramsEnd(String boundary) {
        return "--" + boundary + "--" + "\r\n";
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
}
