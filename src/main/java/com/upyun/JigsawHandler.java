package com.upyun;


import java.io.IOException;
import java.util.Map;

public class JigsawHandler extends AsyncProcessHandler {


    /**
     * 初始化异步拼图
     *
     * @param bucketName 空间名称
     * @param userName   操作员名称
     * @param password   密码，不需要MD5加密
     */
    public JigsawHandler(String bucketName, String userName, String password) {
        super(bucketName, userName, password);
    }

    /**
     * 发起异步拼图请求
     *
     * @param params 请求参数
     * @return 请求结果
     * @throws IOException
     */
    public Result process(Map<String, Object> params) throws IOException, UpException {
        return super.process(params);
    }


    public class Params {
        /**
         * 请求参数
         * <p>
         * bucket_name	string	是	文件所在空间名称
         * notify_url	string	是	回调通知地址
         * tasks	string	是	处理任务信息，详见下
         * app_name	string	是	任务所使用的云处理程序，文件拉取为 spiderman
         */
        public final static String BUCKET_NAME = "bucket_name";
        public final static String NOTIFY_URL = "notify_url";
        public final static String TASKS = "tasks";
        public final static String APP_NAME = "app_name";


        /**
         * 回调通知参数
         * <p>
         * task_id	string	任务对应的 TaskId
         * bucket_name	string	文件所在的空间名
         * status_code	integer	处理结果状态码，200 表示成功处理
         * path	string	文件保存路径
         * error	string	处理错误信息描述，空字符串表示没有错误
         */
        public final static String TASK_ID = "task_id";
        //        public final static String BUCKET_NAME = "bucket_name";
        public final static String STATUS_CODE = "status_code";
        public final static String PATH = "path";
        public final static String ERROR = "error";

        /**
         * 处理参数
         *
         * image_matrix	[][]string	是	需要拼接的图片路径和拼接方式
         * save_as	string	是	拼接后图片的保存路径
         * x-gmkerl-thumb	string	否	对拼接后的图片进行图片处理
         *
         */
        public final static String IMAGE_MATRIX = "image_matrix";
        public final static String SAVE_AS = "save_as";
        public final static String X_GMKERL_THUMB = "x-gmkerl-thumb";
        public final static String overwrite = "width";

    }
}
