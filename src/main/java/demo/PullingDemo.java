package demo;

import com.upyun.PullingHandler;
import com.upyun.Result;
import com.upyun.UpException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PullingDemo {

    //     运行前先设置好以下三个参数
    private static final String BUCKET_NAME = "空间名称";
    private static final String OPERATOR_NAME = "操作员名称";
    private static final String OPERATOR_PWD = "操作员密码";


    public static void main(String[] args) {
        testPulling();
    }


    private static void testPulling() {

        PullingHandler handler = new PullingHandler(BUCKET_NAME, OPERATOR_NAME, OPERATOR_PWD);

        //初始化参数组 Map
        Map<String, Object> paramsMap = new HashMap<String, Object>();

        //空间名
        paramsMap.put(PullingHandler.Params.BUCKET_NAME, BUCKET_NAME);
        //回调地址
        paramsMap.put(PullingHandler.Params.NOTIFY_URL, "http://httpbin.org/post");
        //选择任务
        paramsMap.put(PullingHandler.Params.APP_NAME, "spiderman");

        //以json格式生成任务信息
        JSONArray array = new JSONArray();
        JSONObject json = new JSONObject();

        //添加处理参数
        json.put(PullingHandler.Params.URL, "http://formtest.b0.upaiyun.com/sample.jpeg");
        json.put(PullingHandler.Params.RANDOM, false);
        json.put(PullingHandler.Params.OVERWRITE, true);
        json.put(PullingHandler.Params.X_GMKERL_THUMB, "/watermark/text/5L2g5aW977yB");
        json.put(PullingHandler.Params.SAVE_AS, "/result/pulling/a.jpg");

        JSONObject json2 = new JSONObject();

        //添加处理参数
        json2.put(PullingHandler.Params.URL, "http://formtest.b0.upaiyun.com/sample.jpeg");
        json2.put(PullingHandler.Params.RANDOM, false);
        json2.put(PullingHandler.Params.OVERWRITE, true);
        json2.put(PullingHandler.Params.X_GMKERL_THUMB, "/fw/300/unsharp/true/quality/80/format/png");
        json2.put(PullingHandler.Params.SAVE_AS, "/result/pulling/b.jpg");

        array.put(json2);
        array.put(json);

        //添加任务信息
        paramsMap.put(PullingHandler.Params.TASKS, array);

        try {
            Result result = handler.process(paramsMap);
            System.out.println(result);
            if (result.isSucceed()) {

                String[] ids = handler.getTaskId(result.getMsg());
                System.out.println(Arrays.toString(ids));

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UpException e) {
            e.printStackTrace();
        }

    }
}
