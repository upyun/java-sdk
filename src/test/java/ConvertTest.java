import com.upyun.ConvertHandler;
import com.upyun.ConvertHandler;
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

public class ConvertTest {
    private static final String BUCKET_NAME = "formtest";
    private static final String OPERATOR_NAME = "one";
    private static final String OPERATOR_PWD = "qwertyuiop";


    @Test
    public void testConvert() {

        ConvertHandler handler = new ConvertHandler(BUCKET_NAME, OPERATOR_NAME, OPERATOR_PWD);

        //初始化参数组 Map
        Map<String, Object> paramsMap = new HashMap<String, Object>();

        paramsMap.put(ConvertHandler.Params.BUCKET_NAME, BUCKET_NAME);
        paramsMap.put(ConvertHandler.Params.NOTIFY_URL, "http://httpbin.org/post");
        paramsMap.put(ConvertHandler.Params.APP_NAME, "uconvert");

        //已json格式生成任务信息
        JSONArray array = new JSONArray();
        JSONObject json = new JSONObject();

        //添加处理参数
        json.put(ConvertHandler.Params.SOURCE, "/test_ppt.pptx");
        json.put(ConvertHandler.Params.SAVE_AS, "/result/convert/convert");

        array.put(json);

        //添加任务信息
        paramsMap.put(ConvertHandler.Params.TASKS, array);

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
