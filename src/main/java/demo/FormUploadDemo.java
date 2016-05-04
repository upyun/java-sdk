package main.java.demo;

import main.java.com.upyun.*;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class FormUploadDemo {
    // 运行前先设置好以下两个参数
    private static final String BUCKET_NAME = "空间名";
    private static final String APIKEY = "表单密匙";

    //上传测试文件
    private static final String SAMPLE_PIC_FILE = System.getProperty("user.dir") + "/sample.jpeg";

    public static void main(String[] args) {
        File file = new File(SAMPLE_PIC_FILE);
        //保存路径 必须设置该参数
        String savePath = "/uploads/{year}{mon}{day}/{random32}{.suffix}";
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
}
