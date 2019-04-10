package com.upyun;

import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ParallelUploader extends BaseUploader {

    private volatile int blockProgress;

    public void setParallel(int parallel) {
        this.parallel = parallel;
    }

    //并行式断点并行数
    private int parallel = 4;

    //分块上传状态 1 成功 2 上传中 3 上传失败
    private int[] status;

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
     * @param uuid   上传任务 uuid
     * @param status 分块上传状态
     * @return
     * @throws IOException
     */
    public boolean resume(String uuid, int[] status) throws IOException, UpException {

        this.uuid = uuid;
        this.status = status;

        if (uuid == null || status == null || status.length != totalBlock - 2) {
            throw new UpException("uuid or status is wrong, please restart!");
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
     * @param password   密码，需要MD5加密
     * @return SerialUploader object
     */
    public ParallelUploader(String bucketName, String userName, String password) {
        super(bucketName, userName, password);
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
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

        if (status == null || status.length != totalBlock - 2 || uuid == null) {
            status = new int[totalBlock - 2];
        }

        this.params.put(X_UPYUN_MULTI_DISORDER, "true");

        return startUpload();
    }

    /**
     * 获取分块状态
     *
     * @return
     */
    public int[] getStatus() {
        return status;
    }

    /**
     * 设置分块状态
     *
     * @param status
     */
    public void setStatus(int[] status) {
        this.status = status;
    }

    boolean processUpload() throws IOException, UpException {

        blockProgress = 0;

        ExecutorService uploadExecutor = Executors.newFixedThreadPool(parallel);

        for (int i = 0; i < totalBlock - 2; i++) {

            Future future = uploadExecutor.submit(uploadBlock(i));

            try {
                future.get();
            } catch (Exception e) {
                uploadExecutor.shutdown();
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                    randomAccessFile = null;
                }
                throw new UpException(e.getMessage());
            }
        }

        uploadExecutor.shutdown();

        try {//等待直到所有任务完成
            uploadExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return completeUpload();
    }

    private Runnable uploadBlock(final int index) {
        return new Runnable() {
            public void run() {

                try {
                    if (paused) {
                        throw new UpException("upload paused");
                    }
                    if (status[index] == 1) {
                        if (onProgressListener != null) {
                            onProgressListener.onProgress(blockProgress + 2, totalBlock);
                        }
                        blockProgress++;
                        return;
                    } else if (status[index] == 2) {
                        return;
                    }

                    status[index] = 2;

                    byte[] data = readBlockByIndex(index);

                    RequestBody requestBody = RequestBody.create(null, data);

                    String date = getGMTDate();

                    String md5 = null;

                    if (checkMD5) {
                        md5 = UpYunUtils.md5(data);
                    }

                    String sign = UpYunUtils.sign("PUT", date, uri, userName, password, md5).trim();

                    Request.Builder builder = new Request.Builder()
                            .url(url)
                            .header(DATE, date)
                            .header(AUTHORIZATION, sign)
                            .header(X_UPYUN_MULTI_STAGE, "upload")
                            .header(X_UPYUN_MULTI_UUID, uuid)
                            .header(X_UPYUN_PART_ID, index + "")
                            .header("User-Agent", UpYunUtils.VERSION)
                            .put(requestBody);

                    if (md5 != null) {
                        builder.header(CONTENT_MD5, md5);
                    }

                    Response response = uploadRequest(builder);

                    uuid = response.header(X_UPYUN_MULTI_UUID, "");
                    status[index] = 1;
                } catch (Exception e) {
                    status[index] = 3;
                    throw new RuntimeException(e.getMessage());
                }
            }
        };

    }

    private Response uploadRequest(Request.Builder builder) {

        try {
            Response response = mClient.newCall(builder.build()).execute();
            if (!response.isSuccessful()) {
                int x_error_code = Integer.parseInt(response.header("X-Error-Code", "-1"));
                if (x_error_code == 40011061 || x_error_code == 40011059) {
                    uuid = null;
                }
                throw new RuntimeException(response.body().string());
            } else {
                if (onProgressListener != null) {
                    onProgressListener.onProgress(blockProgress + 2, totalBlock);
                }
                blockProgress++;
            }
            return response;

        } catch (IOException e) {
            throw new RuntimeException(e.toString());
        }
    }

    boolean completeUpload() throws IOException, UpException {
        completeRequest();
        status = null;
        uuid = null;
        return true;
    }
}
