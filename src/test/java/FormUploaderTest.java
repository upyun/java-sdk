package test.java;

import main.java.com.upyun.*;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class FormUploaderTest {

    private static final String BUCKET_NAME = "formtest";
    private static final String APIKEY = "GqSu2v26RI+Xu3yLdsWfynTS/LM=";

    //上传测试文件
    private static final String SAMPLE_PIC_FILE = System.getProperty("user.dir") + "/sample.jpeg";

    @Test
    public void testUploadFile(){
        File file = new File(SAMPLE_PIC_FILE);
        //保存路径 必须设置该参数
        String savePath = "/uploads/{year}{mon}{day}/{random32}{.suffix}";
        final Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put(Params.SAVE_KEY, savePath);

        FormUploader uploader = new FormUploader(BUCKET_NAME, APIKEY, null);
        Result result = uploader.upload(paramsMap, file);
        assertTrue(result.isSucceed());

        //通过回到凡是获取签名
        SignatureListener signatureListener = new SignatureListener() {
            public String getSignature(String raw) {
                return UpYunUtils.md5(raw + APIKEY);
            }
        };

        FormUploader uploader2 = new FormUploader(BUCKET_NAME, null, signatureListener);
        Result result2 = uploader2.upload(paramsMap, file);
        assertTrue(result2.isSucceed());
    }

    @Test
    public void testUploadByte(){
        //保存路径 必须设置该参数
        String savePath = "/uploads/{year}{mon}{day}/{random32}{.suffix}";
        final Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put(Params.SAVE_KEY, savePath);

        FormUploader uploader = new FormUploader(BUCKET_NAME, APIKEY, null);
        Result result = uploader.upload(paramsMap, "test1".getBytes());
        assertTrue(result.isSucceed());

        //通过回到凡是获取签名
        SignatureListener signatureListener = new SignatureListener() {
            public String getSignature(String raw) {
                return UpYunUtils.md5(raw + APIKEY);
            }
        };

        FormUploader uploader2 = new FormUploader(BUCKET_NAME, null, signatureListener);
        Result result2 = uploader2.upload(paramsMap, "test2".getBytes());
        assertTrue(result2.isSucceed());
    }

}