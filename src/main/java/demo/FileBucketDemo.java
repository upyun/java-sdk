package demo;

import com.UpYun;
import com.upyun.UpException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * 文件类空间的demo
 */
public class FileBucketDemo {

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

    private static UpYun upyun = null;

    static {
        File txtFile = new File(SAMPLE_TXT_FILE);

        if (!txtFile.isFile()) {
            System.out.println("本地待上传的测试文件不存在！");
        }
    }

    public static void main(String[] args) throws IOException, UpException {

        // 初始化空间
        upyun = new UpYun(BUCKET_NAME, OPERATOR_NAME, OPERATOR_PWD);

        // ****** 可选设置 begin ******

        // 切换 API 接口的域名接入点，默认为自动识别接入点
        // upyun.setApiDomain(UpYun.ED_AUTO);

        // 设置连接超时时间，默认为30秒
        // upyun.setTimeout(60);

        // 设置是否开启debug模式，默认不开启
        upyun.setDebug(true);

        // ****** 可选设置 end ******

        // 1.创建目录，有两种形式
        testMkDir();

        // 2.上传文件，图片空间的文件上传请参考 PicBucketDemo.java
        testWriteFile();
        // 3.获取文件信息
        testGetFileInfo();

        // 4.读取目录
        testReadDir();

        // 5.获取空间占用大小
        testGetBucketUsage();

        // 6.获取某个目录的空间占用大小
        testGetFolderUsage();

        // 7.读取文件/下载文件
        testReadFile();

        // 8.删除文件
        testDeleteFile();

        // 9.删除目录
        testRmDir();

        // 10.复制文件
        testCopyFile();

        // 9.移动文件
        testMoveFile();
    }

    /**
     * 获取空间占用大小
     */
    public static void testGetBucketUsage() throws IOException, UpException {

        long usage = upyun.getBucketUsage();

        System.out.println("空间总使用量：" + usage + "B");
        System.out.println();
    }

    /**
     * 获取某个目录的空间占用大小
     */
    public static void testGetFolderUsage() throws IOException, UpException {

        // 带查询的目录，如 "/" 或 "/tmp"
        String dirPath = DIR_ROOT;

        long usage = upyun.getFolderUsage(dirPath);

        System.out.println("'" + dirPath + "'目录占用量： " + usage + "B");
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

        /*
         * 上传方法1：文本内容直接上传
         */
        boolean result1 = upyun.writeFile(filePath, content);
        System.out.println("1.上传 " + filePath + isSuccess(result1));

        /*
         * 上传方法2：文本内容直接上传，可自动创建父级目录（最多10级）
         */
        boolean result2 = upyun.writeFile(filePath2, content, true);
        System.out.println("2.上传 " + filePath2 + isSuccess(result2));

        /*
         * 上传方法3：采用数据流模式上传文件（节省内存），可自动创建父级目录（最多10级）
         */
        File file = new File(SAMPLE_TXT_FILE);
        boolean result3 = upyun.writeFile(filePath, file, true);
        System.out.println("3.上传 " + filePath + isSuccess(result3));

        /*
         * 上传方法4：对待上传的文件设置 MD5 值，确保上传到 Upyun 的文件的完整性和正确性
         */
        File file4 = new File(SAMPLE_TXT_FILE);
        // 设置待上传文件的 Content-MD5 值
        // 如果又拍云服务端收到的文件MD5值与用户设置的不一致，将回报 406 NotAcceptable 错误
        upyun.setContentMD5(UpYun.md5(file4));

        boolean result4 = upyun.writeFile(filePath, file4, true);
        System.out.println("4.上传 " + filePath + isSuccess(result4));
        System.out.println();

    }

    /**
     * 获取文件信息
     */
    public static void testGetFileInfo() throws IOException, UpException {

        // upyun空间下存在的文件的路径
        String filePath = DIR_ROOT + FILE_NAME;

        System.out.println(filePath + " 的文件信息：" + upyun.getFileInfo(filePath));
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
         * 方法1：直接读取文本内容
         */
        String datas = upyun.readFile(filePath);
        System.out.println(filePath + " 的文件内容:" + datas);

        /*
         * 方法2：下载文件，采用数据流模式下载文件（节省内存）
         */
        // 要写入的本地临时文件
        File file = File.createTempFile("upyunTempFile_", "");

        // 把upyun空间下的文件下载到本地的临时文件
        boolean result = upyun.readFile(filePath, file);
        System.out.println(filePath + " 下载" + isSuccess(result) + "，保存到 "
                + file.getAbsolutePath());
        System.out.println();
    }

    /**
     * 删除文件
     */
    public static void testDeleteFile() throws IOException, UpException {

        // upyun空间下存在的文件的路径
        String filePath = DIR_ROOT + FILE_NAME;

        // 删除文件
        boolean result = upyun.deleteFile(filePath, null);

        System.out.println(filePath + " 删除" + isSuccess(result));
        System.out.println();
    }

    /**
     * 创建目录
     */
    public static void testMkDir() throws IOException, UpException {

        // 方法1：创建一级目录
        String dir1 = DIR_ROOT + FOLDER_NAME;

        boolean result1 = upyun.mkDir(dir1);
        System.out.println("创建目录：" + dir1 + isSuccess(result1));

        // 方法2：创建多级目录，自动创建父级目录（最多10级）
        String dir2 = DIR_MORE + FOLDER_NAME;

        boolean result2 = upyun.mkDir(dir2, true);
        System.out.println("自动创建多级目录：" + dir2 + isSuccess(result2));
        System.out.println();
    }

    /**
     * 读取目录下的文件列表
     */
    public static void testReadDir() throws IOException, UpException {

        // 参数可以换成其他目录路径
        String dirPath = DIR_ROOT;

        Map<String, String> params = new HashMap<String, String>();

        params.put(UpYun.PARAMS.KEY_X_LIST_LIMIT.getValue(), "10");

        // 读取目录列表，将返回 List 或 NULL
//        List<UpYun.FolderItem> items = upyun.readDir(dirPath, params);
//
//        if (null == items) {
//            System.out.println("'" + dirPath + "'目录下没有文件。");
//
//        } else {
//
//
//            for (int i = 0; i < items.size(); i++) {
//                System.out.println(items.get(i));
//            }
//
//            System.out.println("'" + dirPath + "'目录总共有 " + items.size()
//                    + " 个文件。");
//        }

        UpYun.FolderItemIter folderItemIter = upyun.readDirIter(dirPath, params);
        System.out.println(folderItemIter);

        String json = upyun.readDirJson(dirPath, params);
        System.out.println(json);

        System.out.println();
    }

    /**
     * 删除目录
     */
    public static void testRmDir() throws IOException, UpException {

        // 带删除的目录必须存在，并且目录下已不存在任何文件或子目录
        String dirPath = DIR_MORE + FOLDER_NAME;

        boolean result = upyun.rmDir(dirPath);

        System.out.println("删除目录：" + dirPath + isSuccess(result));
        System.out.println();
    }


    /**
     * 复制文件
     *
     * @throws IOException
     * @throws UpException
     */
    public static void testCopyFile() throws IOException, UpException {
        String filePath = DIR_ROOT + FILE_NAME;

        boolean result = upyun.copyFile("/test.aa", "/" + BUCKET_NAME + filePath);

        assertTrue(result);
    }

    /**
     * 移动文件
     *
     * @throws IOException
     * @throws UpException
     */
    public static void testMoveFile() throws IOException, UpException {
        String filePath = DIR_ROOT + FILE_NAME;

        boolean result = upyun.moveFile("/test.aa", "/" + BUCKET_NAME + filePath);

        assertTrue(result);
    }

    private static String isSuccess(boolean result) {
        return result ? " 成功" : " 失败";
    }
}
