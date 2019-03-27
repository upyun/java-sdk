package demo;

import com.upyun.BaseUploader;
import com.upyun.SerialUploader;
import com.upyun.UpException;

import java.io.IOException;

public class SerialUploadDemo {

    //     运行前先设置好以下三个参数
    private static final String BUCKET_NAME = "空间名称";
    private static final String OPERATOR_NAME = "操作员名称";
    private static final String OPERATOR_PWD = "操作员密码";

    //上传测试文件
    private static final String SAMPLE_PIC_FILE = System.getProperty("user.dir") + "/test.MOV";

    //上传至空间路径
    private static final String UPLOAD_PATH = "/test.MOV";

    public static void main(String[] args) throws IOException, UpException, InterruptedException {

        final SerialUploader resume = new SerialUploader(BUCKET_NAME, OPERATOR_NAME, OPERATOR_PWD);

        //设置上传进度监听
        resume.setOnProgressListener(new BaseUploader.OnProgressListener() {
            public void onProgress(int index, int total) {
                System.out.println(index + "::" + total + "::" + index * 100 / total + "%");
            }
        });

        //设置 MD5 校验
        resume.setCheckMD5(true);


        new Thread() {
            public void run() {
                try {
                    System.out.println("first:" + resume.upload(SAMPLE_PIC_FILE, UPLOAD_PATH, null));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (UpException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        Thread.sleep(2000);

        resume.pause();

        Thread.sleep(2000);

        System.out.println(resume.resume());
    }
}
