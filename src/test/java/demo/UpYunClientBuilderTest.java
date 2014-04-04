package demo;

import com.upyun.Crypto;
import com.upyun.UpYunClient;
import org.junit.Test;

import java.io.File;

/**
 * User: zjzhai
 * Date: 4/2/14
 */
public class UpYunClientBuilderTest {


    // 运行前先设置好以下三个参数
    private static final String BUCKET_NAME = "afiletest";
    private static final String USER_NAME = "root1";
    private static final String USER_PWD = "a123123123";

    /**
     * 根目录
     */
    private static final String DIR_ROOT = "/";
    /**
     * 多级目录
     */
    private static final String DIR_MORE = "/1/2/3/";
    /**
     * 目录名
     */
    private static final String FOLDER_NAME = "tmp1";
    /**
     * 上传到upyun的文件名
     */
    private static final String FILE_NAME = "test.txt";

    /**
     * 本地待上传的测试文件
     */
    private static final String SAMPLE_TXT_FILE;


    static {
        SAMPLE_TXT_FILE = UpYunClientBuilderTest.class.getResource("/test.txt").getFile();
    }


    @Test
    public void testName() throws Exception {
        UpYunClient builder = UpYunClient.create(BUCKET_NAME, USER_NAME, USER_PWD);

        // 方法2：创建多级目录，自动创建父级目录（最多10级）
        String dir2 = DIR_MORE + FOLDER_NAME;

        builder.recursionMkDir().createFolder(dir2);



        // 要传到upyun后的文件路径
        String filePath = DIR_ROOT + FILE_NAME;
        // 要传到upyun后的文件路径：多级目录
        String filePath2 = DIR_MORE + FILE_NAME;

        File file = new File(SAMPLE_TXT_FILE);

        builder.recursionMkDir().contentMD5(Crypto.md5(file)).uploadFile(filePath, file);


    }
}
