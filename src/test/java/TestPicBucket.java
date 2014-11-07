package test.java;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import main.java.com.UpYun;
import main.java.com.UpYun.PARAMS;

import org.junit.Test;

public class TestPicBucket {
	// 运行前先设置好以下三个参数
	private static final String BUCKET_NAME = "sdkimg";
	private static final String OPERATOR_NAME = "tester";
	private static final String OPERATOR_PWD = "grjxv2mxELR3";

	/** 根目录 */
	private static final String DIR_ROOT = "/";

	/** 上传到upyun的图片名 */
	private static final String PIC_NAME = "sample.jpeg";

	/** 本地待上传的测试文件 */
	private static final String SAMPLE_PIC_FILE = System
			.getProperty("user.dir") + "/sample.jpeg";
	UpYun upyun = new UpYun(BUCKET_NAME, OPERATOR_NAME, OPERATOR_PWD);

	// 测试图片文件测试
	@Test
	public void testWritePic() throws IOException {
		// 要传到upyun后的文件路径
		String filePath = DIR_ROOT + PIC_NAME;

		// 本地待上传的图片文件
		File file = new File(SAMPLE_PIC_FILE);
		upyun.setTimeout(120);
		// 设置待上传文件的 Content-MD5 值
		// 如果又拍云服务端收到的文件MD5值与用户设置的不一致，将回报 406 NotAcceptable 错误
		upyun.setContentMD5(UpYun.md5(file));

		// 设置待上传文件的"访问密钥"
		// 注意：
		// 仅支持图片空！，设置密钥后，无法根据原文件URL直接访问，需带URL后面加上（缩略图间隔标志符+密钥）进行访问
		// 举例：
		// 如果缩略图间隔标志符为"!"，密钥为"bac"，上传文件路径为"/folder/test.jpg"，
		// 那么该图片的对外访问地址为：http://空间域名 /folder/test.jpg!bac
		upyun.setFileSecret("bac");

		// 上传文件，并自动创建父级目录（最多10级）
		boolean result = upyun.writeFile(filePath, file, true);
		assertTrue(result);

		// 图片宽度
		String width = upyun.getPicWidth();
		// 图片高度
		String height = upyun.getPicHeight();
		// 图片帧数
		String frames = upyun.getPicFrames();
		// 图片类型
		String type = upyun.getPicType();

		assertTrue(width != null && !"".equals(width));
		assertTrue(height != null && !"".equals(height));
		assertTrue(frames != null && !"".equals(frames));
		assertTrue(type != null && !"".equals(type));

	}

	// 制作图片缩略图测试
	@Test
	public void testGmkerl() throws IOException {
		// 要传到upyun后的文件路径
		String filePath = DIR_ROOT + "gmkerl.jpg";

		// 本地待上传的图片文件
		File file = new File(SAMPLE_PIC_FILE);

		// 设置缩略图的参数
		Map<String, String> params = new HashMap<String, String>();

		// 设置缩略图类型，必须搭配缩略图参数值（KEY_VALUE）使用，否则无效
		params.put(PARAMS.KEY_X_GMKERL_TYPE.getValue(),
				PARAMS.VALUE_FIX_BOTH.getValue());

		// 设置缩略图参数值，必须搭配缩略图类型（KEY_TYPE）使用，否则无效
		params.put(PARAMS.KEY_X_GMKERL_VALUE.getValue(), "150x150");

		// 设置缩略图的质量，默认 95
		params.put(PARAMS.KEY_X_GMKERL_QUALITY.getValue(), "95");

		// 设置缩略图的锐化，默认锐化（true）
		params.put(PARAMS.KEY_X_GMKERL_UNSHARP.getValue(), "true");

		// 若在 upyun 后台配置过缩略图版本号，则可以设置缩略图的版本名称
		// 注意：只有存在缩略图版本名称，才会按照配置参数制作缩略图，否则无效
		params.put(PARAMS.KEY_X_GMKERL_THUMBNAIL.getValue(), "small");

		// 上传文件，并自动创建父级目录（最多10级）
		boolean result = upyun.writeFile(filePath, file, true, params);

		assertTrue(result);
	}

	// 图片旋转测试
	@Test
	public void testRotate() throws IOException {
		// 要传到upyun后的文件路径
		String filePath = DIR_ROOT + "rotate.jpg";

		// 本地待上传的图片文件
		File file = new File(SAMPLE_PIC_FILE);

		// 图片旋转功能具体可参考：http://wiki.upyun.com/index.php?title=图片旋转
		Map<String, String> params = new HashMap<String, String>();

		// 设置图片旋转：只接受"auto"，"90"，"180"，"270"四种参数
		params.put(PARAMS.KEY_X_GMKERL_ROTATE.getValue(),
				PARAMS.VALUE_ROTATE_90.getValue());

		// 上传文件，并自动创建父级目录（最多10级）
		boolean result = upyun.writeFile(filePath, file, true, params);

		assertTrue(result);
	}

	// 图片裁剪测试
	@Test
	public void testGrop() throws IOException {
		// 要传到upyun后的文件路径
		String filePath = DIR_ROOT + "crop.jpg";

		// 本地待上传的图片文件
		File file = new File(SAMPLE_PIC_FILE);

		// 图片裁剪功能具体可参考：http://wiki.upyun.com/index.php?title=图片裁剪
		Map<String, String> params = new HashMap<String, String>();

		// 设置图片裁剪，参数格式：x,y,width,height
		params.put(PARAMS.KEY_X_GMKERL_CROP.getValue(), "50,50,300,300");

		// 上传文件，并自动创建父级目录（最多10级）
		boolean result = upyun.writeFile(filePath, file, true, params);

		assertTrue(result);
	}
}
