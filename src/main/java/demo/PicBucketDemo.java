package demo;

import com.upyun.RestManager;
import com.upyun.UpException;
import com.upyun.UpYunUtils;
import okhttp3.Response;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 图片类空间的demo，一般性操作参考文件空间的demo（FileBucketDemo.java）
 * <p>
 * 注意：直接使用部分图片处理功能后，将会丢弃原图保存处理后的图片
 */
public class PicBucketDemo {

    // 运行前先设置好以下三个参数
    private static final String BUCKET_NAME = "空间名称";
    private static final String OPERATOR_NAME = "操作员名称";
    private static final String OPERATOR_PWD = "操作员密码";

    /**
     * 根目录
     */
    private static final String DIR_ROOT = "/";

    /**
     * 上传到upyun的图片名
     */
    private static final String PIC_NAME = "sample.jpeg";

    /**
     * 本地待上传的测试文件
     */
    private static final String SAMPLE_PIC_FILE = System
            .getProperty("user.dir") + "/sample.jpeg";

    static {
        File picFile = new File(SAMPLE_PIC_FILE);

        if (!picFile.isFile()) {
            System.out.println("本地待上传的测试文件不存在!");
        }
    }

    public static void main(String[] args) throws IOException, UpException {

        // 初始化空间
        RestManager restManager = new RestManager(BUCKET_NAME, OPERATOR_NAME, OPERATOR_PWD);

        // 要传到upyun后的文件路径
        String filePath = DIR_ROOT + PIC_NAME;

        // 本地待上传的图片文件
        File file = new File(SAMPLE_PIC_FILE);

        Map<String, String> params = new HashMap<String, String>();
        // 设置待上传文件的 Content-MD5 值
        // 如果又拍云服务端收到的文件MD5值与用户设置的不一致，将回报 406 NotAcceptable 错误
        params.put(RestManager.PARAMS.CONTENT_MD5.getValue(), UpYunUtils.md5(file, 1024));

        // 设置待上传文件的"访问密钥"
        // 注意：
        // 仅支持图片空！，设置密钥后，无法根据原文件URL直接访问，需带URL后面加上（缩略图间隔标志符+密钥）进行访问
        // 举例：
        // 如果缩略图间隔标志符为"!"，密钥为"bac"，上传文件路径为"/folder/test.jpg"，
        // 那么该图片的对外访问地址为：http://空间域名 /folder/test.jpg!bac
        params.put(RestManager.PARAMS.CONTENT_SECRET.getValue(), "bac");

        //图片预处理参数
        params.put(RestManager.PARAMS.X_GMKERL_THUMB.getValue(),
                "/fw/300/unsharp/true/rotate/90");

        // 上传文件
        Response response = restManager.writeFile(filePath, file, params);
        System.out.println("图片上传：" + isSuccess(response.isSuccessful()));
    }


    private static String isSuccess(boolean result) {
        return result ? " 成功" : " 失败";
    }
}
