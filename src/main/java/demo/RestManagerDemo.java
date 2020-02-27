package demo;

import com.UpYun;
import com.upyun.RestManager;
import com.upyun.UpException;
import okhttp3.Response;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * 文件类空间的demo
 */
public class RestManagerDemo {

    // 运行前先设置好以下三个参数
    private static final String BUCKET_NAME = "空间名称";
    private static final String OPERATOR_NAME = "操作员名称";
    private static final String OPERATOR_PWD = "操作员密码";

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
    private static final String FOLDER_NAME = "tmp";
    /**
     * 上传到upyun的文件名
     */
    private static final String FILE_NAME = "test.txt";

    /**
     * 本地待上传的测试文件
     */
    private static final String SAMPLE_TXT_FILE = System
            .getProperty("user.dir") + "/test.txt";

    private static RestManager restManager = null;

    static {
        File txtFile = new File(SAMPLE_TXT_FILE);

        if (!txtFile.isFile()) {
            System.out.println("本地待上传的测试文件不存在！");
        }
    }

    public static void main(String[] args) throws IOException, UpException {

        // 初始化空间
        restManager = new RestManager(BUCKET_NAME, OPERATOR_NAME, OPERATOR_PWD);

        // ****** 可选设置 begin ******

        // 切换 API 接口的域名接入点，默认为自动识别接入点
        restManager.setApiDomain(RestManager.ED_AUTO);

        // 设置连接超时时间，默认为30秒
        restManager.setTimeout(60);

        // ****** 可选设置 end ******

        //上传文件，图片空间的文件上传请参考 PicBucketDemo.java
        testWriteFile();

        //复制文件
        testCopyFile();

        //移动文件
        testMoveFile();

        //读取文件/下载文件
        testReadFile();

        //删除文件
        testDeleteFile();

        //创建目录
        testMkDir();

        //删除目录
        testRmDir();

        //获取文件信息
        testGetFileInfo();

        //获取目录文件列表
        testReadDir();

        //获取服务使用量
        testGetBucketUsage();

    }

    /**
     * 获取空间占用大小
     */
    public static void testGetBucketUsage() throws IOException, UpException {

        Response response = restManager.getBucketUsage();

        System.out.println("空间总使用量：" + response.body().string() + "B");
        System.out.println();
    }

    /**
     * 上传文件
     *
     * @throws IOException
     */
    public static void testWriteFile() throws IOException, UpException {

        // 要上传的纯文字内容
        String content = "test content";

        // 要传到upyun后的文件路径
        String filePath = DIR_ROOT + FILE_NAME;
        // 要传到upyun后的文件路径：多级目录
        String filePath2 = DIR_MORE + FILE_NAME;
        String filePath3 = DIR_MORE + "test3.txt";

        /*
         * 上传方法1：上传 byte[]
         */
        Response response = restManager.writeFile(filePath, content.getBytes(), null);
        System.out.println("1.上传 " + filePath + isSuccess(response));


        /*
         * 上传方法2：上传文件
         */
        File file = new File(SAMPLE_TXT_FILE);
        Response response2 = restManager.writeFile(filePath2, file, null);
        System.out.println("2.上传 " + filePath2 + isSuccess(response2));

        /*
         * 上传方法3：上传流
         */
        File file2 = new File(SAMPLE_TXT_FILE);
        Response response3 = restManager.writeFile(filePath3, new FileInputStream(file2), null);
        System.out.println("3.上传 " + filePath3 + isSuccess(response3));
    }

    /**
     * 获取文件信息
     */
    public static void testGetFileInfo() throws IOException, UpException {

        // upyun空间下存在的文件的路径
        String filePath = DIR_MORE + "test3.txt";

        System.out.println(filePath + " 的文件信息：" + restManager.getFileInfo(filePath).headers());
        System.out.println();
    }

    /**
     * 读取文件/下载文件
     *
     * @throws IOException
     */
    public static void testReadFile() throws IOException, UpException {

        // upyun空间下存在的文件的路径
        String filePath = DIR_ROOT + FILE_NAME;

        /*
         * 下载文件
         */
        Response response = restManager.readFile(filePath);
        System.out.println(filePath + " 的文件内容:" + response.body().string());
    }

    /**
     * 删除文件
     */
    public static void testDeleteFile() throws IOException, UpException {

        // upyun空间下存在的文件的路径
        String filePath = DIR_ROOT + FILE_NAME;

        // 删除文件
        Response response = restManager.deleteFile(filePath, null);

        System.out.println(filePath + " 删除" + isSuccess(response));
        System.out.println();
    }

    /**
     * 创建目录
     */
    public static void testMkDir() throws IOException, UpException {
        String dir1 = DIR_ROOT + FOLDER_NAME;
        Response response = restManager.mkDir(dir1);
        System.out.println("创建目录：" + dir1 + isSuccess(response));


    }

    /**
     * 删除目录
     */
    public static void testRmDir() throws IOException, UpException {

        // 带删除的目录必须存在，并且目录下已不存在任何文件或子目录
        String dirPath = DIR_ROOT + FOLDER_NAME;
        Response response = restManager.rmDir(dirPath);
        System.out.println("删除目录：" + dirPath + isSuccess(response));
    }

    /**
     * 读取目录下的文件列表
     */
    public static void testReadDir() throws IOException, UpException {

        // 参数可以换成其他目录路径
        String dirPath = DIR_ROOT;

        Map<String, String> params = new HashMap<String, String>();

        params.put(UpYun.PARAMS.KEY_X_LIST_LIMIT.getValue(), "10");

        Response response = restManager.readDirIter(dirPath, params);
        System.out.println(response.body().string());
    }


    /**
     * 复制文件
     *
     * @throws IOException
     * @throws UpException
     */
    public static void testCopyFile() throws IOException, UpException {
        String sourcePath = "/" + BUCKET_NAME + DIR_ROOT + FILE_NAME;
        Response response = restManager.copyFile("/copy.txt", sourcePath, null);
        System.out.println("复制文件：" + sourcePath + isSuccess(response));
    }

    /**
     * 移动文件
     *
     * @throws IOException
     * @throws UpException
     */
    public static void testMoveFile() throws IOException, UpException {
        String sourcePath = "/" + BUCKET_NAME + DIR_MORE + FILE_NAME;
        Response response = restManager.moveFile("/move.txt", sourcePath, null);
        System.out.println("移动文件：" + sourcePath + isSuccess(response));
    }

    private static String isSuccess(Response response) {
        return response.isSuccessful() ? " 成功" : " 失败";
    }
}
