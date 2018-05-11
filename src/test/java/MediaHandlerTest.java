import com.upyun.MediaHandler;
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

public class MediaHandlerTest {

    private static final String BUCKET_NAME = "formtest";
    private static final String OPERATOR_NAME = "one";
    private static final String OPERATOR_PWD = "qwertyuiop";


    @Test
    public void testMediaProcess() {
        MediaHandler handler = new MediaHandler(BUCKET_NAME, OPERATOR_NAME, OPERATOR_PWD);

        //初始化参数组 Map
        Map<String, Object> paramsMap = new HashMap<String, Object>();

        paramsMap.put(MediaHandler.Params.BUCKET_NAME, BUCKET_NAME);
        paramsMap.put(MediaHandler.Params.NOTIFY_URL, "http://httpbin.org/post");
        paramsMap.put(MediaHandler.Params.ACCEPT, "json");
        paramsMap.put(MediaHandler.Params.SOURCE, "/test.mp4");

        JSONArray array = new JSONArray();

        JSONObject json = new JSONObject();

        json.put(MediaHandler.Params.TYPE, "video");
        json.put(MediaHandler.Params.AVOPTS, "/s/240p(4:3)/as/1/r/30");
        json.put(MediaHandler.Params.RETURN_INFO, "true");
        json.put(MediaHandler.Params.SAVE_AS, "testProcess.mp4");

        JSONObject json2 = new JSONObject();

        json2.put(MediaHandler.Params.TYPE, "video");
        json2.put(MediaHandler.Params.AVOPTS, "/s/240p(4:3)/as/1/r/30");
        json2.put(MediaHandler.Params.RETURN_INFO, "true");
        json2.put(MediaHandler.Params.SAVE_AS, "testProcess2.mp4");

        array.put(json2);
        array.put(json);

        paramsMap.put(MediaHandler.Params.TASKS, array);

        Result result = null;
        try {
            result = handler.process(paramsMap);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (UpException e) {
            e.printStackTrace();
        }

        assertNotNull(result);
        assertTrue(result.isSucceed());
        assertNotNull(result.getMsg());

        String[] ids = handler.getTaskId(result.getMsg());
        assertNotNull(ids);
    }

    @Test
    public void testMediaStatus() {
        String[] ids = new String[]{"02774703e7f5cc855681a7c42d819722", "921110d3843e92ac7932ff216e5ea348"};

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

        Result result = null;
        try {
            result = handle.getStatus(paramsMap);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (UpException e) {
            e.printStackTrace();
        }

        assertNotNull(result);
        assertTrue(result.isSucceed());
        assertNotNull(result.getMsg());
    }

    @Test
    public void testMediaResult() {
        String[] ids = new String[]{"02774703e7f5cc855681a7c42d819722", "921110d3843e92ac7932ff216e5ea348"};

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

        Result result = null;
        try {
            result = handle.getStatus(paramsMap);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (UpException e) {
            e.printStackTrace();
        }

        assertNotNull(result);
        assertTrue(result.isSucceed());
        assertNotNull(result.getMsg());
    }


}
