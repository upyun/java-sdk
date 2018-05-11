package demo;

import com.upyun.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;

public class FormUploadDemo {
    // 运行前先设置好以下三个参数
    private static final String BUCKET_NAME = "formtest";
    private static final String OPERATOR_NAME = "one";
    private static final String OPERATOR_PWD = "qwertyuiop";


    //上传测试文件
    private static final String SAMPLE_PIC_FILE = System.getProperty("user.dir") + "/sample.jpeg";


    private static File file = new File(SAMPLE_PIC_FILE);
    //保存路径 必须设置该参数
    private static String savePath = "/uploads/{year}{mon}{day}/{random32}{.suffix}";

    public static void main(String[] args) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {

        testWriteFile();
        testWatermark();
        testSync();
        testAsync();
    }

    /**
     * 测试上传文件
     */
    private static void testWriteFile() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        final Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put(Params.SAVE_KEY, savePath);

        FormUploader uploader = new FormUploader(BUCKET_NAME, OPERATOR_NAME, OPERATOR_PWD);
        Result result = uploader.upload(paramsMap, file);
        System.out.println(result);

        System.out.println(uploader.upload(paramsMap, "test1".getBytes()));
    }

    /**
     * 测试上传图片添加水印
     */

    private static void testWatermark() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        FormUploader uploader = new FormUploader(BUCKET_NAME, OPERATOR_NAME, OPERATOR_PWD);

        final Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put(Params.SAVE_KEY, savePath);
        paramsMap.put(Params.X_GMKERL_THUMB, "/watermark/text/5L2g5aW977yB");

        System.out.println(uploader.upload(paramsMap, file));
    }

    /**
     * 测试上传同步作图
     */

    private static void testSync() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        //初始化uploader
        FormUploader uploader = new FormUploader(BUCKET_NAME, OPERATOR_NAME, OPERATOR_PWD);

        //初始化参数组 Map
        final Map<String, Object> paramsMap = new HashMap<String, Object>();

        //添加 SAVE_KEY 参数
        paramsMap.put(Params.SAVE_KEY, savePath);

        //添加同步上传作图参数 X_GMKERL_THUMB
        paramsMap.put(Params.X_GMKERL_THUMB, "/fw/300/unsharp/true/quality/80/format/png");

        //打印结果
        System.out.println(uploader.upload(paramsMap, file));
    }

    /**
     * 测试上传异步作图
     */

    private static void testAsync() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        //uploader
        FormUploader uploader = new FormUploader(BUCKET_NAME, OPERATOR_NAME, OPERATOR_PWD);

        //初始化参数组 Map
        final Map<String, Object> paramsMap = new HashMap<String, Object>();

        //添加 SAVE_KEY 参数
        paramsMap.put(Params.SAVE_KEY, savePath);

        //初始化JSONArray
        JSONArray array = new JSONArray();

        //初始化JSONObject
        JSONObject json = new JSONObject();

        //json 添加 name 属性
        json.put("name", "thumb");

        //json 添加 X_GMKERL_THUMB 属性
        json.put(Params.X_GMKERL_THUMB, "/fw/300/unsharp/true/quality/80/format/png");

        //json 添加 save_as 属性
        json.put("save_as", "/path/to/fw_100.jpg");

        //json 添加 notify_url 属性
        json.put("notify_url", "http://httpbin.org/post");

        //将json 对象放入 JSONArray
        array.put(json);

        //添加异步作图参数 APPS
        paramsMap.put(Params.APPS, array);

        //打印结果
        System.out.println(uploader.upload(paramsMap, file));
    }

}
