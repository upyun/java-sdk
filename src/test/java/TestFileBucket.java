package test.java;

import static org.junit.Assert.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import main.java.com.UpYun;
import org.junit.Test;

public class TestFileBucket {
	private static final String BUCKET_NAME = "sdkfile";
	private static final String OPERATOR_NAME = "tester";
	private static final String OPERATOR_PWD = "grjxv2mxELR3";
	
	private UpYun upyun = new UpYun(BUCKET_NAME, OPERATOR_NAME, OPERATOR_PWD);

	/** 根目录 */
	private static final String DIR_ROOT = "/";
	/** 多级目录 */
	private static final String DIR_MORE = "/1/2/3/";
	/** 目录名 */
	private static final String FOLDER_NAME = "tmp";
	/** 上传到upyun的文件名 */
	private static final String FILE_NAME = "test.txt";
	/** 本地待上传的测试文件 */
	private static final String SAMPLE_TXT_FILE = System.getProperty("user.dir") + "/test.txt";

	private static final String content = "tmp content";
	
	
	//创建目录测试
	@Test
	public void testMkDir() {
		// 方法1：创建一级目录
		String dir1 = DIR_ROOT + FOLDER_NAME;

		boolean result1 = upyun.mkDir(dir1);
		assertTrue(result1);

		// 方法2：创建多级目录，自动创建父级目录（最多10级）
		String dir2 = DIR_MORE + FOLDER_NAME;

		boolean result2 = upyun.mkDir(dir2, true);
		assertTrue(result2);
	}

	//上传普通文件测试
	@Test
	public void testWriteFile() throws IOException {
		// 保存在又拍云的文件路径
		String filePath = DIR_ROOT + FILE_NAME;
		// 保存在又拍云的文件路径：多级目录
		String filePath2 = DIR_MORE + FILE_NAME;

		/*
		 * 上传方法1：文本内容直接上传
		 */
		boolean result1 = upyun.writeFile(filePath, content);
		assertTrue(result1);

		/*
		 * 上传方法2：文本内容直接上传，可自动创建父级目录（最多10级）
		 */
		boolean result2 = upyun.writeFile(filePath2, content, true);
		assertTrue(result2);

		/*
		 * 上传方法3：采用数据流模式上传文件（节省内存），可自动创建父级目录（最多10级）
		 */
		File file = new File(SAMPLE_TXT_FILE);
		boolean result3 = upyun.writeFile(filePath, file, true);
		assertTrue(result3);

		/*
		 * 上传方法4：对待上传的文件设置 MD5 值，确保上传到 Upyun 的文件的完整性和正确性
		 */
		File file4 = new File(SAMPLE_TXT_FILE);
		// 设置待上传文件的 Content-MD5 值
		// 如果又拍云服务端收到的文件MD5值与用户设置的不一致，将回报 406 NotAcceptable 错误
		upyun.setContentMD5(UpYun.md5(file4));

		boolean result4 = upyun.writeFile(filePath, file4, true);
		assertTrue(result4);

	}

	//获取文件信息测试
	@Test
	public void testGetFileInfo() {
		// upyun空间下存在的文件的路径
		String filePath = DIR_ROOT + FILE_NAME;

		Map<String,String> result = upyun.getFileInfo(filePath);
		assertTrue(result.size() == 3);
	}

	//读取目录文件列表测试
	@Test
	public void testReadDir() {

		// 参数可以换成其他目录路径
		String dirPath = DIR_ROOT;

		// 读取目录列表，将返回 List 或 NULL
		List<UpYun.FolderItem> items = upyun.readDir(dirPath);

		assertTrue(items.size() > 0);
	}

	//获取空间使用量测试
	@Test
	public void testGetBucketUsage() {
		long usage = upyun.getBucketUsage();

		assertTrue(usage > 0);
	}

	//获取目录大小信息测试
	@Test
	public void testGetFolderUsage() {
		// 带查询的目录，如 "/" 或 "/tmp"
		String dirPath = DIR_ROOT;

		long usage = upyun.getFolderUsage(dirPath);

		assertTrue(usage > 0);
	}

	//读取文件测试
	@Test
	public void testReadFile() throws IOException {
		// upyun空间下存在的文件的路径
		String filePath = DIR_ROOT + FILE_NAME;

		/*
		 * 方法1：直接读取文本内容
		 */
		String datas = upyun.readFile(filePath);
		assertTrue(content.equals(datas));

		/*
		 * 方法2：下载文件，采用数据流模式下载文件（节省内存）
		 */
		// 要写入的本地临时文件
		File file = File.createTempFile("upyunTempFile_", "");

		// 把upyun空间下的文件下载到本地的临时文件
		boolean result = upyun.readFile(filePath, file);
		assertTrue(result);
	}

	//删除文件测试
	@Test
	public void testDeleteFile() {
		// upyun空间下存在的文件的路径
		String filePath = DIR_ROOT + FILE_NAME;

		// 删除文件
		boolean result = upyun.deleteFile(filePath);

		assertTrue(result);
	}

	//删除目录测试
	@Test
	public void testRmDir() {
		// 带删除的目录必须存在，并且目录下已不存在任何文件或子目录
		String dirPath = DIR_MORE + FOLDER_NAME;

		boolean result = upyun.rmDir(dirPath);

		assertTrue(result);
	}
}
