package com.upyun;

import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public abstract class BaseUploader {
    static final String AUTHORIZATION = "Authorization";
    static final int BLOCK_SIZE = 1024 * 1024;

    final String DATE = "Date";

    static final String CONTENT_MD5 = "Content-MD5";
    static final String CONTENT_TYPE = "CContent-Type";
    static final String CONTENT_SECRET = "Content-Secret";
    static final String X_Upyun_Meta_X = "X-Upyun-Meta-X";

    static final String X_UPYUN_MULTI_DISORDER = "X-Upyun-Multi-Disorder";
    static final String X_UPYUN_MULTI_STAGE = "X-Upyun-Multi-Stage";
    static final String X_UPYUN_MULTI_TYPE = "X-Upyun-Multi-Type";
    static final String X_UPYUN_MULTI_LENGTH = "X-Upyun-Multi-Length";
    static final String X_UPYUN_MULTI_UUID = "X-Upyun-Multi-UUID";
    static final String X_UPYUN_PART_ID = "X-Upyun-Part-ID";
    static final String X_UPYUN_NEXT_PART_ID = "X-Upyun-Next-Part-ID";

    static final String HOST = "https://v0.api.upyun.com";

    Map<String, String> params;

    volatile boolean paused;

    String uuid;
    String uri;
    OkHttpClient mClient;
    File mFile;
    RandomAccessFile randomAccessFile;

    boolean checkMD5;

    // 空间名
    String bucketName;
    // 操作员名
    String userName;
    // 操作员密码
    String password;
    //超时设置(s)
    int timeout = 20;

    String url;

    OnProgressListener onProgressListener;

    int totalBlock;

    /**
     * 初始化 SerialUploader
     *
     * @param bucketName 空间名称
     * @param userName   操作员名称
     * @param password   密码，需要MD5加密
     * @return SerialUploader object
     */
    public BaseUploader(String bucketName, String userName, String password) {
        this.bucketName = bucketName;
        this.userName = userName;
        this.password = UpYunUtils.md5(password);
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void pause() {
        this.paused = true;
    }

    void init(String filePath, String uploadPath, Map<String, String> params) throws IOException {

        this.paused = false;

        if (params == null) {
            params = new HashMap<String, String>();
        }

        this.params = params;

        this.mFile = new File(filePath);

        this.totalBlock = (int) Math.ceil(mFile.length() / (double) BLOCK_SIZE + 2);

        this.url = HOST + UpYunUtils.formatPath(bucketName, uploadPath);

        this.uri = HttpUrl.get(url).encodedPath();

        this.mClient = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 获取 uuid
     *
     * @return uuid
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * 设置 uuid
     *
     * @param uuid
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * 设置是否 MD5 校验
     *
     * @param checkMD5 是否 MD5 校验
     */
    public void setCheckMD5(boolean checkMD5) {
        this.checkMD5 = checkMD5;
    }

    /**
     * 设置上传进度监听
     *
     * @param onProgressListener 上传进度 listener
     */
    public void setOnProgressListener(OnProgressListener onProgressListener) {
        this.onProgressListener = onProgressListener;
    }

    boolean startUpload() throws IOException, UpException {

        if (paused) {
            throw new UpException("upload paused");
        }

        if (uuid == null) {
            RequestBody requestBody = RequestBody.create(null, "");

            String date = UpYunUtils.getGMTDate();

            String md5 = null;

            if (checkMD5) {
                md5 = UpYunUtils.md5("");
            }

            String sign = UpYunUtils.sign("PUT", date, uri, userName, password, md5).trim();

            Request.Builder builder = new Request.Builder()
                    .url(url)
                    .header(DATE, date)
                    .header(AUTHORIZATION, sign)
                    .header(X_UPYUN_MULTI_STAGE, "initiate")
                    .header(X_UPYUN_MULTI_TYPE, "application/octet-stream")
                    .header(X_UPYUN_MULTI_LENGTH, mFile.length() + "")
                    .header("User-Agent", UpYunUtils.VERSION)
                    .put(requestBody);

            if (params != null) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    builder.header(entry.getKey(), entry.getValue());
                }
            }

            if (md5 != null) {
                builder.header(CONTENT_MD5, md5);
            }
            callRequest(builder.build(), 1);
        }

        if (this.randomAccessFile == null) {
            this.randomAccessFile = new RandomAccessFile(mFile, "r");
        }

        return processUpload();
    }

    abstract boolean processUpload() throws IOException, UpException;

    abstract boolean completeUpload() throws IOException, UpException;

    void completeRequest() throws UpException, IOException {

        if (randomAccessFile != null) {
            randomAccessFile.close();
            randomAccessFile = null;
        }

        RequestBody requestBody = RequestBody.create(null, "");

        String date = UpYunUtils.getGMTDate();

        String md5 = null;

        if (checkMD5) {
            md5 = UpYunUtils.md5("");
        }

        String sign = UpYunUtils.sign("PUT", date, uri, userName, password, md5).trim();

        Request.Builder builder = new Request.Builder()
                .url(url)
                .header(DATE, date)
                .header(AUTHORIZATION, sign)
                .header(X_UPYUN_MULTI_STAGE, "complete")
                .header(X_UPYUN_MULTI_UUID, uuid)
                .header("User-Agent", UpYunUtils.VERSION)
                .put(requestBody);

        if (md5 != null) {
            builder.header(CONTENT_MD5, md5);
        }

        callRequest(builder.build(), totalBlock);
    }

    Response callRequest(Request request, int index) throws IOException, UpException {

        Response response = mClient.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new UpException(response.body().string());
        } else {
            if (onProgressListener != null) {
                onProgressListener.onProgress(index, totalBlock);
            }
        }

        uuid = response.header(X_UPYUN_MULTI_UUID, "");

        return response;
    }

    byte[] readBlockByIndex(long index) throws IOException {
        byte[] block = new byte[BLOCK_SIZE];
        int readedSize = 0;
        long offset = index * BLOCK_SIZE;
        randomAccessFile.seek(offset);
        readedSize = randomAccessFile.read(block, 0, BLOCK_SIZE);

        // read last block, adjust byte size
        if (readedSize < BLOCK_SIZE) {
            byte[] notFullBlock = new byte[readedSize];
            System.arraycopy(block, 0, notFullBlock, 0, readedSize);
            return notFullBlock;
        }
        return block;
    }

    public interface OnProgressListener {
        void onProgress(int index, int total);
    }
}
