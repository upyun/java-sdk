import com.upyun.SerialUploader;
import com.upyun.UpException;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class ResumeTest {
    private static final String BUCKET_NAME = "formtest";
    private static final String OPERATOR_NAME = "one";
    private static final String OPERATOR_PWD = "qwertyuiop";


    //上传测试文件
    private static final String SAMPLE_PIC_FILE = System.getProperty("user.dir") + "/test.MOV";

    //上传至空间路径
    private static final String UPLOAD_PATH = "/test.MOV";

    @Test
    public void testResumeUpload() throws InterruptedException, IOException, UpException {

        final SerialUploader resume = new SerialUploader(BUCKET_NAME, OPERATOR_NAME, OPERATOR_PWD);
        final CountDownLatch latch = new CountDownLatch(1);

        //设置上传进度监听
        resume.setOnProgressListener(new SerialUploader.OnProgressListener() {
            public void onProgress(int index, int total) {
                Assert.assertTrue(index <= total);
                System.out.println(index + "::" + total + "::" + index * 100 / total + "%");
            }
        });

        //设置 MD5 校验
        resume.setCheckMD5(true);

        resume.upload(SAMPLE_PIC_FILE, UPLOAD_PATH, null);

//        new Thread() {
//            @Override
//            public void run() {
//                super.run();
//                //开始上传
//                try {
//                    Assert.assertFalse(resume.upload(SAMPLE_PIC_FILE, UPLOAD_PATH, null));
//                    latch.countDown();
//                } catch (Exception e) {
//                    System.out.println(e);
//                }
//            }
//        }.start();

//        Thread.sleep(2000);
//
//        resume.interrupt(new SerialUploader.OnInterruptListener() {
//            public void OnInterrupt(boolean interrupted) {
//
//                System.out.println("interrupted:" + interrupted);
//                if (interrupted) {
//                    try {
//                        Assert.assertTrue(resume.upload(SAMPLE_PIC_FILE, UPLOAD_PATH, null));
//                    } catch (IOException e) {
//                        Assert.fail();
//                    } catch (UpException e) {
//                        Assert.fail();
//                    }
//                }
//            }
//        });
//
//        latch.await();
    }

}
