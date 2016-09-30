package main.java.demo;

import main.java.com.upyun.ResumeUploader;
import main.java.com.upyun.UpException;

import java.io.IOException;

public class ResumeUploadDemo {

    //     运行前先设置好以下三个参数
    private static final String BUCKET_NAME = "空间名称";
    private static final String OPERATOR_NAME = "操作员名称";
    private static final String OPERATOR_PWD = "操作员密码";

    //上传测试文件
    private static final String SAMPLE_PIC_FILE = System.getProperty("user.dir") + "/test.MOV";

    //上传至空间路径
    private static final String UPLOAD_PATH = "/test.MOV";

    public static void main(String[] args) {

        final ResumeUploader resume = new ResumeUploader(BUCKET_NAME, OPERATOR_NAME, OPERATOR_PWD);

        //设置上传进度监听
        resume.setOnProgressListener(new ResumeUploader.OnProgressListener() {
            public void onProgress(int index, int total) {
                System.out.println(index + "::" + total + "::" + index * 100 / total + "%");
            }
        });

        //设置 MD5 校验
        resume.setCheckMD5(true);

        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    try {
                        System.out.println(resume.upload(SAMPLE_PIC_FILE, UPLOAD_PATH, null));
                    } catch (UpException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                resume.interrupt();
            }
        }.start();


        try {
            Thread.sleep(10000);
            System.out.println(resume.upload(SAMPLE_PIC_FILE, UPLOAD_PATH, null));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
