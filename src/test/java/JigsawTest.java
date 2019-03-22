import com.upyun.JigsawHandler;
import com.upyun.JigsawHandler;
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

public class JigsawTest {
    private static final String BUCKET_NAME = "formtest";
    private static final String OPERATOR_NAME = "one";
    private static final String OPERATOR_PWD = "qwertyuiop";


    @Test
    public void testJigsaw() {

        JigsawHandler handler = new JigsawHandler(BUCKET_NAME, OPERATOR_NAME, OPERATOR_PWD);

        //初始化参数组 Map
        Map<String, Object> paramsMap = new HashMap<String, Object>();

        paramsMap.put(JigsawHandler.Params.BUCKET_NAME, BUCKET_NAME);
        paramsMap.put(JigsawHandler.Params.NOTIFY_URL, "http://httpbin.org/post");
        paramsMap.put(JigsawHandler.Params.APP_NAME, "jigsaw");

        //已json格式生成任务信息
        JSONArray array = new JSONArray();
        JSONObject json = new JSONObject();


        String[][] pigs = new String[1][3];

        pigs[0][0] = "/result/convert/convert-00.png";
        pigs[0][1] = "/result/convert/convert-01.png";
        pigs[0][2] = "/result/convert/convert-03.png";

        //添加处理参数
        json.put(JigsawHandler.Params.IMAGE_MATRIX, pigs);
        json.put(JigsawHandler.Params.SAVE_AS, "/result/jigsaw/a.jpg");

        array.put(json);

        //添加任务信息
        paramsMap.put(JigsawHandler.Params.TASKS, array);

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
