package demo;

import com.upyun.ParallelUploader;
import com.upyun.ResumeUploader;
import com.upyun.UpException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class ParallelUploadDemo {

    //     运行前先设置好以下三个参数
    private static final String BUCKET_NAME = "空间名称";
    private static final String OPERATOR_NAME = "操作员名称";
    private static final String OPERATOR_PWD = "操作员密码";

    //上传测试文件
    private static final String SAMPLE_PIC_FILE = System.getProperty("user.dir") + "/test.MOV";

    //上传至空间路径
    private static final String UPLOAD_PATH = "/test.MOV";

    public static void main(String[] args) throws InterruptedException, IOException, UpException, ExecutionException {

        ParallelUploader parallelUploader = new ParallelUploader(BUCKET_NAME, OPERATOR_NAME, OPERATOR_PWD);

        //设置上传进度监听
        parallelUploader.setOnProgressListener(new ResumeUploader.OnProgressListener() {
            public void onProgress(int index, int total) {
                System.out.println(index + "::" + total + "::" + index * 100 / total + "%");
            }
        });

        System.out.println("first:" + parallelUploader.upload(SAMPLE_PIC_FILE, UPLOAD_PATH, null));
    }
}