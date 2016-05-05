package main.java.demo;

import main.java.com.upyun.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class FormUploadDemo {
    // 运行前先设置好以下两个参数
    private static final String BUCKET_NAME = "空间名";
    private static final String APIKEY = "表单密匙";

    //上传测试文件
    private static final String SAMPLE_PIC_FILE = System.getProperty("user.dir") + "/sample.jpeg";


    private static File file = new File(SAMPLE_PIC_FILE);
    //保存路径 必须设置该参数
    private static String savePath = "/uploads/{year}{mon}{day}/{random32}{.suffix}";

    public static void main(String[] args) {

        testWriteFile();
        testWatermark();
        testSync();
        testAsync();
    }

    /**
     * 测试上传文件
     */
    private static void testWriteFile() {
        final Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put(Params.SAVE_KEY, savePath);

        FormUploader uploader = new FormUploader(BUCKET_NAME, APIKEY, null);
        Result result = uploader.upload(paramsMap, file);
        System.out.println(result);

        //通过回到凡是获取签名
        SignatureListener signatureListener = new SignatureListener() {
            public String getSignature(String raw) {
                return UpYunUtils.md5(raw + APIKEY);
            }
        };

        FormUploader uploader2 = new FormUploader(BUCKET_NAME, null, signatureListener);
        Result result2 = uploader2.upload(paramsMap, file);
        System.out.println(result2);

        System.out.println(uploader.upload(paramsMap, "test1".getBytes()));
    }

    /**
     * 测试上传图片添加水印
     */

    private static void testWatermark() {
        FormUploader uploader = new FormUploader(BUCKET_NAME, APIKEY, null);

        final Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put(Params.SAVE_KEY, savePath);
        paramsMap.put(Params.X_GMKERL_THUMB, "/watermark/text/5L2g5aW977yB");

        System.out.println(uploader.upload(paramsMap, file));
    }

    /**
     * 测试上传同步作图
     */

    private static void testSync() {
        FormUploader uploader = new FormUploader(BUCKET_NAME, APIKEY, null);

        final Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put(Params.SAVE_KEY, savePath);
        paramsMap.put(Params.X_GMKERL_THUMB, "/fw/300/unsharp/true/quality/80/format/png");

        System.out.println(uploader.upload(paramsMap, file));
    }

    /**
     * 测试上传异步作图
     */

    private static void testAsync() {
        FormUploader uploader = new FormUploader(BUCKET_NAME, APIKEY, null);

        final Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put(Params.SAVE_KEY, savePath);

        JSONArray array = new JSONArray();
        JSONObject json = new JSONObject();

        json.put("name", "thumb");
        json.put(Params.X_GMKERL_THUMB, "/fw/300/unsharp/true/quality/80/format/png");
        json.put("save_as", "/path/to/fw_100.jpg");
        json.put("notify_url","http://httpbin.org/post");

        array.put(json);
        paramsMap.put(Params.APPS, array);
        System.out.println(uploader.upload(paramsMap, file));
    }

}
