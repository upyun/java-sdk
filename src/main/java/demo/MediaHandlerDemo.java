package demo;

import com.upyun.MediaHandler;
import com.upyun.Result;
import com.upyun.UpException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MediaHandlerDemo {
    //     运行前先设置好以下三个参数
    private static final String BUCKET_NAME = "空间名称";
    private static final String OPERATOR_NAME = "操作员名称";
    private static final String OPERATOR_PWD = "操作员密码";

    public static void main(String[] args) {
        String[] ids = testMediaProcess();

        if (ids != null) {
            testMediaStatus(ids);
            testMediaResult(ids);
        }
    }


    /**
     * 发起异步影视频处理请求DEMO
     *
     * @return TaskId 数组
     */
    private static String[] testMediaProcess() {

        //初始化 MediaHandler
        MediaHandler handler = new MediaHandler(BUCKET_NAME, OPERATOR_NAME, OPERATOR_PWD);

        //初始化参数组 Map
        Map<String, Object> paramsMap = new HashMap<String, Object>();

        //添加必选参数 bucket_name, notify_url, source, tasks, accept
        //空间名
        paramsMap.put(MediaHandler.Params.BUCKET_NAME, BUCKET_NAME);
        //回调地址
        paramsMap.put(MediaHandler.Params.NOTIFY_URL, "http://httpbin.org/post");
        //必须指定为 json
        paramsMap.put(MediaHandler.Params.ACCEPT, "json");
        //需要处理视频路径
        paramsMap.put(MediaHandler.Params.SOURCE, "/test.mp4");


        //已json格式生成任务信息
        JSONArray array = new JSONArray();
        JSONObject json = new JSONObject();

        //添加处理参数
        //不同的处理任务对应不同的 type
        json.put(MediaHandler.Params.TYPE, "video");
        //影视频处理参数 包括视频转码,HLS 切片,视频水印,视频截图,视频拼接,音频剪辑,元数据获取 请见API文档
        json.put(MediaHandler.Params.AVOPTS, "/s/240p(4:3)/as/1/r/30");
        //是否返回 JSON 格式元数据，默认 false。支持 type 值为 video 功能
        json.put(MediaHandler.Params.RETURN_INFO, "true");
        //输出文件保存路径（同一个空间下），如果没有指定，系统自动生成在同空间同目录下
        json.put(MediaHandler.Params.SAVE_AS, "testProcess.mp4");

        JSONObject json2 = new JSONObject();

        json2.put(MediaHandler.Params.TYPE, "video");
        json2.put(MediaHandler.Params.AVOPTS, "/s/240p(4:3)/as/1/r/30");
        json2.put(MediaHandler.Params.RETURN_INFO, "true");
        json2.put(MediaHandler.Params.SAVE_AS, "testProcess2.mp4");

        array.put(json2);
        array.put(json);

        //添加任务信息
        paramsMap.put(MediaHandler.Params.TASKS, array);

        try {
            Result result = handler.process(paramsMap);
            System.out.println(result);
            if (result.isSucceed()) {

                String[] ids = handler.getTaskId(result.getMsg());
                System.out.println(Arrays.toString(ids));
                return ids;

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UpException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 查询处理进度DEMO
     *
     * @param ids 需要查询的任务ID
     */
    private static void testMediaStatus(String[] ids) {

        MediaHandler handle = new MediaHandler(BUCKET_NAME, OPERATOR_NAME, OPERATOR_PWD);

        //初始化参数组 Map
        Map<String, Object> paramsMap = new HashMap<String, Object>();

        paramsMap.put(MediaHandler.Params.BUCKET_NAME, BUCKET_NAME);

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < ids.length; i++) {
            sb.append(ids[i] + ",");
        }

        String task_ids = sb.toString().substring(0, sb.length() - 1);
        paramsMap.put(MediaHandler.Params.TASK_IDS, task_ids);

        try {
            Result result = handle.getStatus(paramsMap);
            System.out.println("status:" + result);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (UpException e) {
            e.printStackTrace();
        }

    }

    /**
     * 查询处理结果DEMO
     *
     * @param ids 需要查询的任务ID
     */
    private static void testMediaResult(String[] ids) {

        MediaHandler handle = new MediaHandler(BUCKET_NAME, OPERATOR_NAME, OPERATOR_PWD);

        //初始化参数组 Map
        Map<String, Object> paramsMap = new HashMap<String, Object>();

        paramsMap.put(MediaHandler.Params.BUCKET_NAME, BUCKET_NAME);

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < ids.length; i++) {
            sb.append(ids[i] + ",");
        }

        String task_ids = sb.toString().substring(0, sb.length() - 1);
        paramsMap.put(MediaHandler.Params.TASK_IDS, task_ids);

        try {
            Result result = handle.getResult(paramsMap);
            System.out.println("result:" + result);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (UpException e) {
            e.printStackTrace();
        }

    }

}
