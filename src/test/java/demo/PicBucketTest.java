package demo;

import com.upyun.*;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * 图片类空间的demo，一般性操作参考文件空间的demo（FileBucketDemo.java）
 * <p/>
 * 注意：直接使用部分图片处理功能后，将会丢弃原图保存处理后的图片
 */
public class PicBucketTest {

    // 运行前先设置好以下三个参数
    private static final String BUCKET_NAME = "apictest";
    private static final String USER_NAME = "root1";
    private static final String USER_PWD = "a123123123";


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
    private static final String SAMPLE_PIC_FILE;


    private static UpYunClient upYunClient;

    static {
        SAMPLE_PIC_FILE = PicBucketTest.class.getResource("/sample.jpeg").getFile();
    }

    @Test
    public void test() throws Exception {

        // 初始化空间
        upYunClient = UpYunClient.create(BUCKET_NAME, USER_NAME, USER_PWD);

        // 设置是否开启debug模式，默认不开启
        upYunClient.enableDebug();

        // ****** 可选设置 end ******

		/*
         * 一般性操作参考文件空间的demo（FileBucketDemo.java）
		 *
		 * 注：图片的所有参数均可以自由搭配使用
		 */

        // 1.上传文件（文件内容）
        testWriteFile();

        // 2.图片做缩略图；若使用了该功能，则会丢弃原图
        testGmkerl();

        // 3.图片旋转；若使用了该功能，则会丢弃原图
        testRotate();

        // 4.图片裁剪；若使用了该功能，则会丢弃原图
        testCrop();

    }

    /**
     * 上传文件
     *
     * @throws java.io.IOException
     */
    public void testWriteFile() throws IOException {

        // 要传到upyun后的文件路径
        String filePath = DIR_ROOT + PIC_NAME;

        // 本地待上传的图片文件
        File file = new File(SAMPLE_PIC_FILE);

        // 上传文件，并自动创建父级目录（最多10级）
        PictureItem pictureItem = upYunClient.recursionMkDir().uploadPicture(filePath, file);

        assert pictureItem.getWidth() == 640;

        assert pictureItem.getHeight() == 427;

        assert "JPEG".equals(pictureItem.getType());

    }

    /**
     * 图片做缩略图
     * <p/>
     * 注意：若使用了缩略图功能，则会丢弃原图
     *
     * @throws java.io.IOException
     */
    public static void testGmkerl() throws IOException {

        // 要传到upyun后的文件路径
        String filePath = DIR_ROOT + "gmkerl.jpg";

        // 本地待上传的图片文件
        File file = new File(SAMPLE_PIC_FILE);


        PictureParamsBuilder builder = PictureParamsBuilder.create();
        builder.picThumbnail(ThumbnailType.VALUE_FIX_BOTH, 150, 150)
                .picThumbnailQuality(95)
                .picThumbnailSharpen()
                .picThumbnailName("small");


        // 上传文件，并自动创建父级目录（最多10级）
        PictureItem pictureItem = upYunClient.recursionMkDir()
                .uploadPicture(filePath, file, builder);


        assert pictureItem.getHeight() == 150;
        assert pictureItem.getWidth() == 150;


    }

    /**
     * 图片旋转
     *
     * @throws java.io.IOException
     */
    public void testRotate() throws IOException {

        // 要传到upyun后的文件路径
        String filePath = DIR_ROOT + "rotate.jpg";

        // 本地待上传的图片文件
        File file = new File(SAMPLE_PIC_FILE);

        // 图片旋转功能具体可参考：http://wiki.upyun.com/index.php?title=图片旋转
        PictureParamsBuilder builder = PictureParamsBuilder.createPicRotateAngle(PictureRotateAngle._90);

        // 上传文件，并自动创建父级目录（最多10级）
        PictureItem pictureItem = upYunClient.uploadPicture(filePath, file, builder);

        assert pictureItem.getWidth() == 427;

        assert pictureItem.getHeight() == 640;


    }

    /**
     * 图片裁剪
     *
     * @throws java.io.IOException
     */
    public void testCrop() throws IOException {

        // 要传到upyun后的文件路径
        String filePath = DIR_ROOT + "crop.jpg";

        // 本地待上传的图片文件
        File file = new File(SAMPLE_PIC_FILE);

        // 图片裁剪功能具体可参考：http://wiki.upyun.com/index.php?title=图片裁剪
        // 设置图片裁剪，参数格式：x,y,width,height

        PictureParamsBuilder builder = PictureParamsBuilder.createPicCutCutting(50, 50, 300, 300);

        // 上传文件，并自动创建父级目录（最多10级）
        PictureItem pictureItem = upYunClient.uploadPicture(filePath, file, builder);

        assert pictureItem.getHeight() == 300;

        assert pictureItem.getWidth() == 300;

    }

}
