package com.upyun;

import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.Map;

public class SerialUploader extends BaseUploader {

    private int nextPartIndex;

    /**
     * 断点续传
     *
     * @return 是否上传成功
     * @throws IOException
     */
    public boolean resume() throws IOException, UpException {
        this.paused = false;
        return startUpload();
    }

    /**
     * 断点续传
     *
     * @param uuid          上传任务 uuid
     * @param nextPartIndex 下一个上传分块 index
     * @return
     * @throws IOException
     */
    public boolean resume(String uuid, int nextPartIndex) throws IOException, UpException {

        this.uuid = uuid;
        this.nextPartIndex = nextPartIndex;

        if (uuid == null) {
            throw new UpException("uuid is null, please restart!");
        } else {
            this.paused = false;
            return startUpload();
        }
    }

    /**
     * 初始化 SerialUploader
     *
     * @param bucketName 空间名称
     * @param userName   操作员名称
     * @param password   密码，不需要MD5加密
     * @return SerialUploader object
     */
    public SerialUploader(String bucketName, String userName, String password) {
        super(bucketName, userName, password);
    }

    /**
     * 开始上传
     *
     * @param filePath   本地上传文件路径
     * @param uploadPath 上传服务器路径
     * @param params     通用上传参数（见 rest api 文档）
     * @return 是否上传成功
     * @throws IOException
     */
    public boolean upload(String filePath, String uploadPath, Map<String, String> params) throws IOException, UpException {
        init(filePath, uploadPath, params);
        return startUpload();
    }

    /**
     * 获取下一个上传分块 index
     *
     * @return index
     */
    public int getNextPartIndex() {
        return nextPartIndex;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setNextPartIndex(int nextPartIndex) {
        this.nextPartIndex = nextPartIndex;
    }

    boolean processUpload() throws IOException, UpException {
        byte[] data;

        while (nextPartIndex >= 0) {

            if (paused) {
                throw new UpException("upload paused");
            }

            data = readBlockByIndex(nextPartIndex);

            RequestBody requestBody = RequestBody.create(null, data);

            String date = UpYunUtils.getGMTDate();

            String md5 = null;

            if (checkMD5) {
                md5 = UpYunUtils.md5(data);
            }

            String sign = UpYunUtils.sign("PUT", date, uri, userName, password, md5);

            Request.Builder builder = new Request.Builder()
                    .url(url)
                    .header(DATE, date)
                    .header(AUTHORIZATION, sign.trim())
                    .header(X_UPYUN_MULTI_STAGE, "upload")
                    .header(X_UPYUN_MULTI_UUID, uuid)
                    .header(X_UPYUN_PART_ID, nextPartIndex + "")
                    .header("User-Agent", UpYunUtils.VERSION)
                    .put(requestBody);

            if (md5 != null) {
                builder.header(CONTENT_MD5, md5);
            }

            if (onProgressListener != null) {
                onProgressListener.onProgress(nextPartIndex + 2, totalBlock);
            }
            callProcessRequest(builder.build());
        }

        return completeUpload();
    }

    boolean completeUpload() throws IOException, UpException {
        completeRequest();
        uuid = null;
        return true;
    }

    private void callProcessRequest(Request request) throws IOException, UpException {

        Response response = mClient.newCall(request).execute();
        if (!response.isSuccessful()) {
            int x_error_code = Integer.parseInt(response.header("X-Error-Code", "-1"));
            if (x_error_code != 40011061 && x_error_code != 40011059) {
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                    randomAccessFile = null;
                }
                uuid = null;
                throw new UpException(response.body().string());
            } else {
                nextPartIndex = Integer.parseInt(response.header(X_UPYUN_NEXT_PART_ID, "-2"));
                return;
            }
        }

        uuid = response.header(X_UPYUN_MULTI_UUID, "");
        nextPartIndex = Integer.parseInt(response.header(X_UPYUN_NEXT_PART_ID, "-2"));
    }

}
