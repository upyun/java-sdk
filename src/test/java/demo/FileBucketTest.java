package demo;

import com.upyun.Crypto;
import com.upyun.FileItem;
import com.upyun.UpYunClient;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 文件类空间的demo
 */

public class FileBucketTest {

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
    private static final String DIR_MORE = "/1/2/4/";
    /**
     * 目录名
     */
    private static final String FOLDER_NAME = "tm66";
    /**
     * 上传到upyun的文件名
     */
    private static final String FILE_NAME = "test.txt";

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
    public void testCreateFolder() {
        // 方法1：创建一级目录
        String dir1 = "/k";
        client.unRecursionMkDir().createFolder(dir1);

        // 方法2：创建多级目录，自动创建父级目录（最多10级）
        String dir2 = "/ssf/sss/ddd";
        client.recursionMkDir().createFolder(dir2);

    }


    //  @Test
    public void test() throws Exception {


        // ****** 可选设置 begin ******

        // 切换 API 接口的域名接入点，默认为自动识别接入点
        // upyun.setApiDomain(UpYun.ED_AUTO);

        // 设置连接超时时间，默认为30秒
        // upyun.timeout(60);

        // 设置是否开启debug模式，默认不开启
        client.enableDebug();
        // ****** 可选设置 end ******

        // 1.创建目录，有两种形式
        testMkDir();

        // 3.获取文件信息
        //  testGetFileInfo();

        // 4.读取目录
        //  testReadDir();

        // 5.获取空间占用大小
        //  testGetBucketUsage();

        // 7.读取文件/下载文件
        //  testReadFile();

        // 8.删除文件
        //testDeleteFile();

        // 9.删除目录
        //testRmDir();
    }

    /**
     * 获取空间占用大小
     */
    public static void testGetBucketUsage() {

        long usage = client.getBucketUsage();

        System.out.println("空间总使用量：" + usage + "B");
        System.out.println();
    }

    /**
     * 上传文件
     *
     * @throws java.io.IOException
     */
    @Test
    public void testUploadFile() throws IOException {

        // 要上传的纯文字内容
        String content = "test content";

        // 要传到upyun后的文件路径
        String filePath = DIR_ROOT + FILE_NAME;
        // 要传到upyun后的文件路径：多级目录
        String filePath2 = DIR_MORE + FILE_NAME;

		/*
         * 上传方法1：文本内容直接上传
		 */
        client.uploadFile(filePath, content);

        assert client.readFileText(filePath).equals("test content");

         /*
         * 上传方法2：文本内容直接上传，可自动创建父级目录（最多10级）
		 */
        client.recursionMkDir().uploadFile(filePath, content);
         /*
         * 上传方法3：采用数据流模式上传文件（节省内存），可自动创建父级目录（最多10级）
		 */
        File file = new File(SAMPLE_TXT_FILE);
        client.recursionMkDir().uploadFile(filePath, file);

	    /*
         * 上传方法4：对待上传的文件设置 MD5 值，确保上传到 Upyun 的文件的完整性和正确性
		 */
        File file4 = new File(SAMPLE_TXT_FILE);
        // 设置待上传文件的 Content-MD5 值
        // 如果又拍云服务端收到的文件MD5值与用户设置的不一致，将回报 406 NotAcceptable 错误
        client.contentMD5(Crypto.md5(file4)).uploadFile(filePath, file4);

    }

    /**
     * 获取文件信息
     */
    public static void testGetFileInfo() {

        // upyun空间下存在的文件的路径
        String filePath = DIR_ROOT + FILE_NAME;

        FileItem item = client.getFileInfo(filePath);
        assert item.getName().equals(FILE_NAME);

    }

    /**
     * 读取文件/下载文件
     *
     * @throws java.io.IOException
     */
    @Test
    public void testReadFile() throws IOException {

        // upyun空间下存在的文件的路径
        String filePath = DIR_ROOT + FILE_NAME;

		/*
         * 方法1：直接读取文本内容
		 */
        String data = client.readFileText(filePath);

        System.out.println(filePath + " 的文件内容:" + data);
        assert "tmp content".equals(data);

		/*
         * 方法2：下载文件，采用数据流模式下载文件（节省内存）
		 */
        // 要写入的本地临时文件
        File file = File.createTempFile("upyunTempFile_", "");

        // 把upyun空间下的文件下载到本地的临时文件
        client.downloadFile(filePath, file);

        assert file.exists();

    }

    /**
     * 删除文件
     */
    @Test
    public void testDeleteFile() {

        // upyun空间下存在的文件的路径
        String filePath = DIR_ROOT + FILE_NAME;

        // 删除文件
        client.deleteFile(filePath);

    }

    /**
     * 创建目录
     */
    @Test
    public void testMkDir() {

        // 方法2：创建多级目录，自动创建父级目录（最多10级）
        String dir2 = DIR_MORE + FOLDER_NAME;
        client.recursionMkDir().createFolder(dir2);


        // 方法1：创建一级目录
        String dir1 = DIR_ROOT + FOLDER_NAME;

        client.unRecursionMkDir().createFolder(dir1);


    }

    /**
     * 读取目录下的文件列表
     */
    @Test
    public void testReadDir() {

        // 参数可以换成其他目录路径
        String dirPath = DIR_ROOT;

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

    /**
     * 删除目录
     */
    @Test
    public void testRmDir() {

        // 带删除的目录必须存在，并且目录下已不存在任何文件或子目录
        String dirPath = DIR_MORE + FOLDER_NAME;

        client.deleteFolder(dirPath);

    }


}
