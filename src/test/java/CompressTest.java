import com.upyun.CompressHandler;
import com.upyun.Result;
import com.upyun.UpException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CompressTest {
    private static final String BUCKET_NAME = "formtest";
    private static final String OPERATOR_NAME = "one";
    private static final String OPERATOR_PWD = "qwertyuiop";

    @Test
    public void testDecompress() {

        CompressHandler handler = new CompressHandler(BUCKET_NAME, OPERATOR_NAME, OPERATOR_PWD);

        //初始化参数组 Map
        Map<String, Object> paramsMap = new HashMap<String, Object>();

        paramsMap.put(CompressHandler.Params.BUCKET_NAME, BUCKET_NAME);
        paramsMap.put(CompressHandler.Params.NOTIFY_URL, "http://httpbin.org/post");
        paramsMap.put(CompressHandler.Params.APP_NAME, "depress");

        //已json格式生成任务信息
        JSONArray array = new JSONArray();
        JSONObject json = new JSONObject();

        //添加处理参数
        json.put(CompressHandler.Params.SOURCES, "/result/compress/a.zip");
        json.put(CompressHandler.Params.SAVE_AS, "/result/depress");

        JSONObject json2 = new JSONObject();

        //添加处理参数
        json2.put(CompressHandler.Params.SOURCES, "/result/compress/b.zip");
        json2.put(CompressHandler.Params.SAVE_AS, "/result/depress2");

        array.put(json2);
        array.put(json);

        //添加任务信息
        paramsMap.put(CompressHandler.Params.TASKS, array);

        try {
            Result result = handler.process(paramsMap);
            assertNotNull(result);
            assertTrue(result.isSucceed());
            assertNotNull(result.getMsg());

            String[] ids = handler.getTaskId(result.getMsg());
            assertNotNull(ids);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UpException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testCompress() {

        CompressHandler handler = new CompressHandler(BUCKET_NAME, OPERATOR_NAME, OPERATOR_PWD);

        //初始化参数组 Map
        Map<String, Object> paramsMap = new HashMap<String, Object>();

        paramsMap.put(CompressHandler.Params.BUCKET_NAME, BUCKET_NAME);
        paramsMap.put(CompressHandler.Params.NOTIFY_URL, "http://httpbin.org/post");
        paramsMap.put(CompressHandler.Params.APP_NAME, "compress");

        //已json格式生成任务信息
        JSONArray array = new JSONArray();
        JSONObject json = new JSONObject();

        JSONArray array2 = new JSONArray();
        array2.put("/a/b/c/sample.jpeg");
        array2.put("/a/b/c/rotate.jpg");

        //添加处理参数
        json.put(CompressHandler.Params.SOURCES, array2);
        json.put(CompressHandler.Params.SAVE_AS, "/result/compress/a.zip");
        json.put(CompressHandler.Params.HOME_DIR, "a/b/c");

        JSONObject json2 = new JSONObject();

        //添加处理参数
        json2.put(CompressHandler.Params.SOURCES, array2);
        json2.put(CompressHandler.Params.SAVE_AS, "/result/compress/b.zip");
        json2.put(CompressHandler.Params.HOME_DIR, "a/b/c");

        array.put(json2);
        array.put(json);

        //添加任务信息
        paramsMap.put(CompressHandler.Params.TASKS, array);

        try {
            Result result = handler.process(paramsMap);

            assertNotNull(result);
            assertTrue(result.isSucceed());
            assertNotNull(result.getMsg());

            String[] ids = handler.getTaskId(result.getMsg());
            assertNotNull(ids);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (UpException e) {
            e.printStackTrace();
        }


    }

}
