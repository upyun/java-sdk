import com.upyun.RestManager;
import com.upyun.RestManager.PARAMS;
import com.upyun.UpException;
import com.upyun.UpYunUtils;
import okhttp3.Response;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class TestPicBucket {
    // 运行前先设置好以下三个参数
    private static final String BUCKET_NAME = "sdkimg";
    private static final String OPERATOR_NAME = "tester";
    private static final String OPERATOR_PWD = "grjxv2mxELR3";

    /**
     * 根目录
     */
    private static final String DIR_ROOT = "/";

    /**
     * 上传到upyun的图片名
     */
    private static final String PIC_NAME = "sample.aaa";

    /**
     * 本地待上传的测试文件
     */
    private static final String SAMPLE_PIC_FILE = System
            .getProperty("user.dir") + "/sample.jpeg";
    RestManager restManager = new RestManager(BUCKET_NAME, OPERATOR_NAME, OPERATOR_PWD);

    // 测试图片文件测试
    @Test
    public void testWritePic() throws IOException, UpException {

        // 要传到upyun后的文件路径
        String filePath = DIR_ROOT + PIC_NAME;

        // 本地待上传的图片文件
        File file = new File(SAMPLE_PIC_FILE);

        Map<String, String> params = new HashMap<String, String>();
        // 设置待上传文件的 Content-MD5 值
        // 如果又拍云服务端收到的文件MD5值与用户设置的不一致，将回报 406 NotAcceptable 错误
        params.put(PARAMS.CONTENT_MD5.getValue(), UpYunUtils.md5(file, 1024));

        // 设置待上传文件的"访问密钥"
        // 注意：
        // 仅支持图片空！，设置密钥后，无法根据原文件URL直接访问，需带URL后面加上（缩略图间隔标志符+密钥）进行访问
        // 举例：
        // 如果缩略图间隔标志符为"!"，密钥为"bac"，上传文件路径为"/folder/test.jpg"，
        // 那么该图片的对外访问地址为：http://空间域名 /folder/test.jpg!bac
        params.put(PARAMS.CONTENT_SECRET.getValue(), "bac");

        //图片预处理参数
        params.put(PARAMS.X_GMKERL_THUMB.getValue(),
                "/fw/300/unsharp/true/rotate/90");

        // 上传文件
        Response response = restManager.writeFile(filePath, file, params);
        assertTrue(response.isSuccessful());

    }
}
