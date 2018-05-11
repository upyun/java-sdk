import com.upyun.*;
import org.junit.Test;

import java.io.File;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class FormUploaderTest {

    private static final String BUCKET_NAME = "formtest";
    private static final String OPERATOR_NAME = "one";
    private static final String OPERATOR_PWD = "qwertyuiop";

    //上传测试文件
    private static final String SAMPLE_PIC_FILE = System.getProperty("user.dir") + "/sample.jpeg";

    @Test
    public void testUploadFile() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        File file = new File(SAMPLE_PIC_FILE);
        //保存路径 必须设置该参数
        String savePath = "/uploads/{year}{mon}{day}/{random32}{.suffix}";
        final Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put(Params.SAVE_KEY, savePath);

        FormUploader uploader = new FormUploader(BUCKET_NAME, OPERATOR_NAME, OPERATOR_PWD);
        Result result = uploader.upload(paramsMap, file);
        assertTrue(result.isSucceed());
    }

    @Test
    public void testUploadByte() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        //保存路径 必须设置该参数
        String savePath = "/uploads/{year}{mon}{day}/{random32}{.suffix}";
        final Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put(Params.SAVE_KEY, savePath);

        FormUploader uploader = new FormUploader(BUCKET_NAME, OPERATOR_NAME, OPERATOR_PWD);
        Result result = uploader.upload(paramsMap, "test1".getBytes());
        assertTrue(result.isSucceed());
    }

}