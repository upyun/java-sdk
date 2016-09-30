package test.java;


import main.java.com.upyun.ResumeUploader;
import main.java.com.upyun.UpException;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class ResumeTest {
    private static final String BUCKET_NAME = "formtest";
    private static final String OPERATOR_NAME = "one";
    private static final String OPERATOR_PWD = "qwertyuiop";


    //上传测试文件
    private static final String SAMPLE_PIC_FILE = System.getProperty("user.dir") + "/test.MOV";

    //上传至空间路径
    private static final String UPLOAD_PATH = "/test.MOV";

    @Test
    public void testResumeUpload() throws InterruptedException {

        final ResumeUploader resume = new ResumeUploader(BUCKET_NAME, OPERATOR_NAME, OPERATOR_PWD);

        //设置上传进度监听
        resume.setOnProgressListener(new ResumeUploader.OnProgressListener() {
            public void onProgress(int index, int total) {
                Assert.assertTrue(index <= total);
                System.out.println(index + "::" + total + "::" + index * 100 / total + "%");
            }
        });

        //设置 MD5 校验
        resume.setCheckMD5(true);

        new Thread() {
            @Override
            public void run() {
                super.run();
                //开始上传
                try {
                    Assert.assertFalse(resume.upload(SAMPLE_PIC_FILE, UPLOAD_PATH, null));
                } catch (IOException e) {
                    Assert.fail();
                } catch (UpException e) {
                    Assert.fail();
                }
            }
        }.start();

        Thread.sleep(1000);

        //终端上传
        resume.interrupt();

        String uuid = resume.getUuid();

        int index = resume.getNextPartIndex();

        try {
            Assert.assertFalse(resume.resume("", 0));
        } catch (IOException e) {
            Assert.fail();
        } catch (UpException e) {
            Assert.assertNotNull(e);
        }

        try {
            Assert.assertFalse(resume.resume());
//            Assert.assertTrue(resume.resume());
        } catch (IOException e) {
            Assert.fail();
        } catch (UpException e) {
            Assert.assertNotNull(e);
        }
        Thread.sleep(2000);

        try {
            Assert.assertTrue(resume.resume(uuid, index));
        } catch (IOException e) {
            Assert.fail();
        } catch (UpException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

}
