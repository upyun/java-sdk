package demo;

import com.upyun.UpYunClient;
import com.upyun.UpYunServerErrorException;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.text.MessageFormat;
import java.util.Date;

/**
 * User: zjzhai
 * Date: 4/4/14
 */
@Ignore
public class SuperTest {

    // 运行前先设置好以下三个参数
    private static final String BUCKET_NAME = "afiletest";
    private static final String USER_NAME = "root1";
    private static final String USER_PWD = "a123123123";

    public static UpYunClient client = new UpYunClient(BUCKET_NAME, USER_NAME, USER_PWD);

    @Ignore
    @Test
    public void testNestedCreateFolder() throws Exception {
        client = new UpYunClient(BUCKET_NAME, USER_NAME, USER_PWD);


        long[] result = new long[3];
        System.out.println(new Date());
        for (int i = 0; i < 3; i++) {
            long l = 0;
            String dir = "";
            try {
                for (; l < 100 * 10; l++) {
                    dir += "/ii" + i;
                    client.recursionMkDir().createFolder(dir);
                }


            } catch (UpYunServerErrorException e) {
                result[i] = l;
                System.out.println(dir);
            }
        }
        System.out.println(new Date());

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < result.length; i++) {
            builder.append(result[i] + ",");
        }
        System.out.println(builder.toString());

    }

    @Ignore
    @Test
    public void testLongFolderName() throws Exception {


        String name = "/";
        long l = 0;
        for (; l < 255; l++) {
            name += "5";
        }

        System.out.println("文件夹名字最长可为：" + (name.length() - 1));
        client.createFolder(name);


    }

    @Test
    public void testBigFile() throws Exception {

        client.recursionMkDir().uploadFile("/mysql.tar.gz", new File("/home/zjzhai/Downloads/4b8c28e842b0134b928287e74fddcc2b?xcode=d20800cb0cf729181cd0b6401e0cc9ab20183b7bfc99c42f"));


    }
}
