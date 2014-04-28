package demo;

import com.upyun.Crypto;
import com.upyun.FileItem;
import com.upyun.UpYunClient;
import com.upyun.UpYunNotFoundException;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

/**
 * 文件类空间的demo
 */

public class FileBucketTest {

    // 运行前先设置好以下三个参数
    private static final String BUCKET_NAME = "apptest1";
    private static final String USER_NAME = "root1";
    private static final String USER_PWD = "a123123123";

    /**
     * 根目录
     */
    private static final String DIR_ROOT = "/";
    /**
     * 多级目录
     */
    private static final String DIR_MORE = "/1/2/4/";
    /**
     * 目录名
     */
    private static final String FOLDER_NAME = "tmdd66";
    /**
     * 上传到upyun的文件名
     */
    private static final String FILE_NAME = "test.tx";

    /**
     * 本地待上传的测试文件
     */
    private static final String SAMPLE_TXT_FILE;

    private static UpYunClient client;

    static {
        SAMPLE_TXT_FILE = FileBucketTest.class.getResource("/test.txt").getFile();
    }

    @Before
    public void setUp() throws Exception {
        // 初始化空间
        client = UpYunClient.create(BUCKET_NAME, USER_NAME, USER_PWD);
    }

    @Test
    public void testMain() throws Exception {
        // 方法1：创建一级目录
        String dir1 = "/a";
        client.unRecursionMkDir().createFolder(dir1);

        // 方法2：创建多级目录，自动创建父级目录（最多10级）
        String dir2 = "/a/b/c/d/";
        client.recursionMkDir().createFolder(dir2);

        /*
         * 文本内容直接上传
		 */
        client.recursionMkDir().uploadFile("/a/a.txt", "test content");

         /*
         * 采用数据流模式上传文件（节省内存）
		 */
        File file = new File(SAMPLE_TXT_FILE);
        client.uploadFile("/a/b.txt", file);

        listFolder();


        // 设置待上传文件的 Content-MD5 值
        // 如果又拍云服务端收到的文件MD5值与用户设置的不一致，将回报 406 NotAcceptable 错误
        client.contentMD5(Crypto.md5(file)).uploadFile("/a/c.txt", file);


        /**
         * 读取空间使用量
         */
        long usage = client.getBucketUsage();
        System.out.println("空间总使用量：" + usage + "B");

        /**
         * 读取文件信息
         */
        FileItem item = client.getFileInfo("/a/a.txt");
        assert item.getName().equals("a.txt");


		/*
         * 直接读取文本内容
		 */
        String data = client.readFileText("/a/a.txt");
        System.out.println(data);
        assert "test content".equals(data);

		/*
         * 下载文件，采用数据流模式下载文件（节省内存）
		 */
        File downloadPath = File.createTempFile("upyunTempFile_", "");
        client.downloadFile("/a/a.txt", downloadPath);
        assert file.exists();


        /**
         * 删除文件
         */
        client.deleteFile("/a/a.txt");
        client.deleteFile("/a/b.txt");
        client.deleteFile("/a/c.txt");


        /**
         * 删除目录
         */
        client.deleteFolder("/a/b/c/d");


    }


    /**
     * 读取目录下的文件列表
     */

    public void listFolder() {

        // 参数可以换成其他目录路径
        String dirPath = "/";

        // 读取目录列表，将返回 List 或 NULL
        List<FileItem> items = client.listFiles(dirPath);

        if (null == items) {
            System.out.println("'" + dirPath + "'目录下没有文件。");

        } else {

            for (int i = 0; i < items.size(); i++) {
                System.out.println(items.get(i));
            }

            System.out.println("'" + dirPath + "'目录总共有 " + items.size()
                    + " 个文件。");
        }

        System.out.println();
    }


    @Test(expected = UpYunNotFoundException.class)
    public void testNotFound() throws Exception {
        client.getFileInfo("/olol/ss.x");
    }

    @Test(expected = UpYunNotFoundException.class)
    public void testDeleteFileAndNotFoundIt() throws Exception {
        client.deleteFile("/a/cc.txt");
    }
}
