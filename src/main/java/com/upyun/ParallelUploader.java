package com.upyun;

import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.*;

public class ParallelUploader {

    private static final String AUTHORIZATION = "Authorization";
    private static final int BLOCK_SIZE = 1024 * 1024;

    private final String DATE = "Date";

    private static final String CONTENT_MD5 = "Content-MD5";
    private static final String CONTENT_TYPE = "CContent-Type";
    private static final String CONTENT_SECRET = "Content-Secret";
    private static final String X_Upyun_Meta_X = "X-Upyun-Meta-X";

    private static final String X_UPYUN_MULTI_DISORDER = "X-Upyun-Multi-Disorder";
    private static final String X_UPYUN_MULTI_STAGE = "X-Upyun-Multi-Stage";
    private static final String X_UPYUN_MULTI_TYPE = "X-Upyun-Multi-Type";
    private static final String X_UPYUN_MULTI_LENGTH = "X-Upyun-Multi-Length";
    private static final String X_UPYUN_MULTI_UUID = "X-Upyun-Multi-UUID";
    private static final String X_UPYUN_PART_ID = "X-Upyun-Part-ID";
    private static final String HOST = "https://v0.api.upyun.com";

    private String uuid;
    private String uploadPath;
    private OkHttpClient mClient;
    private File mFile;
    private RandomAccessFile randomAccessFile;

    private boolean checkMD5;

    // 空间名
    protected String bucketName = null;
    // 操作员名
    protected String userName = null;
    // 操作员密码
    protected String password = null;
    //超时设置(s)
    private int timeout = 20;

    private String url;

    private ResumeUploader.OnProgressListener onProgressListener;

    private int totalBlock;

    private volatile int blockProgress;

    public void setParalle(int paralle) {
        this.paralle = paralle;
    }

    //并行式断点并行数
    private int paralle = 4;

    public void setRetryTime(int retryTime) {
        this.retryTime = retryTime;
    }

    //并行式断点分块重试册数
    private int retryTime = 2;

    /**
     * 初始化 ResumeUploader
     *
     * @param bucketName 空间名称
     * @param userName   操作员名称
     * @param password   密码，需要MD5加密
     * @return ResumeUploader object
     */
    public ParallelUploader(String bucketName, String userName, String password) {
        this.bucketName = bucketName;
        this.userName = userName;
        this.password = UpYunUtils.md5(password);
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
    public boolean upload(String filePath, String uploadPath, Map<String, String> params) throws IOException, UpException, ExecutionException, InterruptedException {

        this.mFile = new File(filePath);

        this.totalBlock = (int) Math.ceil(mFile.length() / (double) BLOCK_SIZE + 2);

        this.randomAccessFile = new RandomAccessFile(mFile, "r");

        this.uploadPath = uploadPath;

        this.url = HOST + "/" + bucketName + uploadPath;

        this.mClient = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .build();

        return startUpload(params);
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
    public void setOnProgressListener(ResumeUploader.OnProgressListener onProgressListener) {
        this.onProgressListener = onProgressListener;
    }

    private boolean startUpload(Map<String, String> params) throws IOException, UpException, ExecutionException, InterruptedException {

        if (uuid != null) {
            return processUpload();
        }

        RequestBody requestBody = RequestBody.create(null, "");

        String date = getGMTDate();

        String md5 = null;

        if (checkMD5) {
            md5 = UpYunUtils.md5("");
        }

        String sign = sign("PUT", date, "/" + bucketName + uploadPath, userName, password, md5).trim();

        Request.Builder builder = new Request.Builder()
                .url(url)
                .header(DATE, date)
                .header(AUTHORIZATION, sign)
                .header(X_UPYUN_MULTI_DISORDER, "true")
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

        return processUpload();
    }

    private boolean processUpload() throws IOException, UpException, InterruptedException, ExecutionException {

        blockProgress = 0;

        ExecutorService uploadExecutor = Executors.newFixedThreadPool(paralle);

        for (int i = 0; i < totalBlock - 2; i++) {
            Future future = uploadExecutor.submit(uploadBlock(i));

            try {
                future.get();
            } catch (InterruptedException e) {
                throw e;
            } catch (ExecutionException e) {
                throw e;
            }
        }

        uploadExecutor.shutdown();

        try {//等待直到所有任务完成
            uploadExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (randomAccessFile != null) {
            randomAccessFile.close();
            randomAccessFile = null;
        }

        return completeUpload();
    }

    private Runnable uploadBlock(final int index) {
        return new Runnable() {
            public void run() {

                try {
                    byte[] data = readBlockByIndex(index);

                    int retry = 0;

                    RequestBody requestBody = RequestBody.create(null, data);

                    String date = getGMTDate();

                    String md5 = null;

                    if (checkMD5) {
                        md5 = UpYunUtils.md5(data);
                    }

                    String sign = sign("PUT", date, "/" + bucketName + uploadPath, userName, password, md5).trim();

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

                    Response response = uploadRequest(retry, builder);

                    uuid = response.header(X_UPYUN_MULTI_UUID, "");

                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage());
                }
            }
        };

    }

    private Response uploadRequest(int retry, Request.Builder builder) {

        try {
            Response response = mClient.newCall(builder.build()).execute();
            if (!response.isSuccessful() && retry < retryTime) {
                return uploadRequest(++retry, builder);
            } else if (!response.isSuccessful() && retry >= retryTime) {
                throw new RuntimeException(response.body().string());
            } else {
                if (onProgressListener != null) {
                    onProgressListener.onProgress(blockProgress + 2, totalBlock);
                }
                blockProgress++;
            }
            return response;

        } catch (IOException e) {
            if (retry < retryTime) {
                return uploadRequest(++retry, builder);
            } else {
                throw new RuntimeException(e.toString());
            }
        }
    }

    private boolean completeUpload() throws IOException, UpException {

        RequestBody requestBody = RequestBody.create(null, "");

        String date = getGMTDate();

        String md5 = null;

        if (checkMD5) {
            md5 = UpYunUtils.md5("");
        }

        String sign = sign("PUT", date, "/" + bucketName + uploadPath, userName, password, md5).trim();

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

        uuid = null;
        return true;
    }

    private void callRequest(Request request, int index) throws IOException, UpException {

        Response response = mClient.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new UpException(response.body().string());
        } else {
            if (onProgressListener != null) {
                onProgressListener.onProgress(index, totalBlock);
            }
        }

        uuid = response.header(X_UPYUN_MULTI_UUID, "");
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

    private String sign(String method, String date, String path, String userName, String password, String md5) throws UpException {

        StringBuilder sb = new StringBuilder();
        String sp = "&";
        sb.append(method);
        sb.append(sp);
//        sb.append("/" + bucket + path);
        sb.append(path);

        sb.append(sp);
        sb.append(date);

        if (md5 != null) {
            sb.append(sp);
            sb.append(md5);
        }
        String raw = sb.toString().trim();
        byte[] hmac = null;
        try {
            hmac = UpYunUtils.calculateRFC2104HMACRaw(password, raw);
        } catch (Exception e) {
            throw new UpException("calculate SHA1 wrong.");
        }

        if (hmac != null) {
            return "UPYUN " + userName + ":" + Base64Coder.encodeLines(hmac);
        }

        return null;
    }

    private byte[] readBlockByIndex(int index) throws IOException {
        byte[] block = new byte[BLOCK_SIZE];
        int readedSize = 0;
        int offset = index * BLOCK_SIZE;
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

    public interface OnInterruptListener {
        void OnInterrupt(boolean interrupted);
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
