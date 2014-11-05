package com;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import com.constant.Constants;
import com.constant.PARAMS;
import com.http.Http;
import com.pojo.FolderItem;
import com.pojo.RequestParams;
import com.pojo.ResponseData;
import com.util.Util;

public class UpYun {
	
	// 空间名
	protected String bucketName = null;
	// 操作员名
	protected String userName = null;
	// 操作员密码
	protected String password = null;
	// 默认不开启debug模式
	public boolean debug = false;
	// 默认的超时时间：30秒
	private int timeout = 30 * 1000;
	// 默认为自动识别接入点
	private String apiDomain = Constants.ED_AUTO;
	// 待上传文件的 Content-MD5 值
	private String contentMD5 = null;
	// 待上传文件的"访问密钥"
	private String fileSecret = null;

	// 图片信息参数
	protected String picWidth = null;
	protected String picHeight = null;
	protected String picFrames = null;
	protected String picType = null;

	// 文件信息参数
	protected String fileType = null;
	protected String fileSize = null;
	protected String fileDate = null;
	
	private Http http = null;

	/**
	 * 初始化 UpYun 存储接口
	 * 
	 * @param bucketName
	 *            空间名称
	 * @param userName
	 *            操作员名称
	 * @param password
	 *            密码，不需要MD5加密
	 * @return UpYun object
	 */
	public UpYun(String bucketName, String userName, String password) {
		this.bucketName = bucketName;
		this.userName = userName;
		this.password = Util.md5(password);
		http = new Http();
	}

	/**
	 * 切换 API 接口的域名接入点
	 * <p>
	 * 可选参数：<br>
	 * 1) UpYun.ED_AUTO(v0.api.upyun.com)：默认，根据网络条件自动选择接入点 <br>
	 * 2) UpYun.ED_TELECOM(v1.api.upyun.com)：电信接入点<br>
	 * 3) UpYun.ED_CNC(v2.api.upyun.com)：联通网通接入点<br>
	 * 4) UpYun.ED_CTT(v3.api.upyun.com)：移动铁通接入点
	 * 
	 * @param domain
	 *            域名接入点
	 */
	public void setApiDomain(String domain) {
		this.apiDomain = domain;
	}

	/**
	 * 查看当前的域名接入点
	 * 
	 * @return
	 */
	public String getApiDomain() {
		return apiDomain;
	}

	/**
	 * 设置连接超时时间，默认为30秒
	 * 
	 * @param second
	 *            秒数，60即为一分钟超时
	 */
	public void setTimeout(int second) {
		this.timeout = second * 1000;
	}

	/**
	 * 查看当前的超时时间
	 * 
	 * @return
	 */
	public int getTimeout() {
		return timeout;
	}

	/**
	 * 查看当前是否是debug模式
	 * 
	 * @return
	 */
	public boolean isDebug() {
		return debug;
	}

	/**
	 * 设置是否开启debug模式
	 * 
	 * @param debug
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	/**
	 * 设置待上传文件的 Content-MD5 值
	 * <p>
	 * 如果又拍云服务端收到的文件MD5值与用户设置的不一致，将回报 406 Not Acceptable 错误
	 * 
	 * @param md5Value
	 *            文件 MD5 校验后的内容
	 */
	public void setContentMD5(String md5Value) {
		this.contentMD5 = md5Value;
	}

	/**
	 * 设置待上传文件的"访问密钥"
	 * <p>
	 * 注意：<br>
	 * 仅支持图片空！设置密钥后，无法根据原文件URL直接访问，需带 URL 后面加上 （缩略图间隔标志符+密钥） 进行访问
	 * <p>
	 * 举例:<br>
	 * 如果缩略图间隔标志符为"!"，密钥为"bac"，上传文件路径为"/folder/test.jpg"，<br>
	 * 那么该图片的对外访问地址为：http://空间域名 /folder/test.jpg!bac
	 * 
	 * @param secret
	 *            密钥字符串
	 */
	public void setFileSecret(String secret) {
		this.fileSecret = secret;
	}

	public String getPicWidth() {
		return picWidth;
	}

	public String getPicHeight() {
		return picHeight;
	}

	public String getPicFrames() {
		return picFrames;
	}

	public String getPicType() {
		return picType;
	}
//
//	/**
//	 * 获取当前SDK的版本号
//	 * 
//	 * @return SDK版本号
//	 */
//	public String version() {
//		return Constants.VERSION;
//	}

	/**
	 * 创建目录
	 * 
	 * @param path
	 *            目录路径
	 * 
	 * @return true or false
	 */
	public boolean mkDir(String path) {
		return mkDir(path, false);
	}

	/**
	 * 创建目录
	 * 
	 * @param path
	 *            目录路径
	 * @param auto
	 *            是否自动创建父级目录(最多10级)
	 * 
	 * @return true or false
	 */
	public boolean mkDir(String path, boolean auto) {

		Map<String, String> params = new HashMap<String, String>(1);
		params.put(PARAMS.KEY_MAKE_DIR.getValue(), "true");
		RequestParams requestParams = new RequestParams();
		requestParams.setMethod(Constants.METHOD_PUT);
		requestParams.setRequestUrl(Util.formatPath(this.bucketName, path));
		requestParams.setAuto(auto);
		requestParams.setParams(params);
		requestParams.setDatas(null);
		
		ResponseData responseData = new ResponseData();
		responseData.setReadFile(false);
		String result = http.HttpAction(this.debug,requestParams,responseData);

		return result != null;
	}
	
	/**
	 * 删除目录
	 * 
	 * @param path
	 *            目录路径
	 * 
	 * @return true or false
	 */
	public boolean rmDir(String path) {
		return HttpAction(Constants.METHOD_DELETE, Util.formatPath(this.bucketName, path)) != null;
	}
	
	/**
	 * 读取目录列表
	 * 
	 * @param path
	 *            目录路径
	 * 
	 * @return List<FolderItem> 或 null
	 */
	public List<FolderItem> readDir(String path) {

		String result = HttpAction(Constants.METHOD_GET, Util.formatPath(this.bucketName, path) + Constants.SEPARATOR);

		if (Util.isEmpty(result))
			return null;

		List<FolderItem> list = new LinkedList<FolderItem>();

		String[] datas = result.split("\n");

		for (int i = 0; i < datas.length; i++) {
			if (datas[i].indexOf("\t") > 0) {
				list.add(new FolderItem(datas[i]));
			}
		}
		return list;
	}
	
	/**
	 * 上传文件
	 * 
	 * @param filePath
	 *            文件路径（包含文件名）
	 * @param datas
	 *            文件内容
	 * 
	 * @return true or false
	 */
	public boolean writeFile(String filePath, byte[] datas) {
		filePath = formatPath(filePath);
		return writeFile(filePath, datas, false, null);
	}

	/**
	 * 上传文件
	 * 
	 * @param filePath
	 *            文件路径（包含文件名）
	 * @param datas
	 *            文件内容
	 * @param auto
	 *            是否自动创建父级目录(最多10级)
	 * 
	 * @return true or false
	 */
	public boolean writeFile(String filePath, byte[] datas, boolean auto) {
		filePath = formatPath(filePath);
		return writeFile(filePath, datas, auto, null);
	}

	/**
	 * 上传文件
	 * 
	 * @param filePath
	 *            文件路径（包含文件名）
	 * @param datas
	 *            文件内容
	 * @param auto
	 *            是否自动创建父级目录(最多10级)
	 * @param params
	 *            额外参数
	 * 
	 * @return true or false
	 */
	public boolean writeFile(String filePath, byte[] datas, boolean auto,
			Map<String, String> params) {

		return HttpAction(Constants.METHOD_PUT, Util.formatPath(this.bucketName, filePath), datas, null, auto,
				params) != null;
	}

	/**
	 * 上传文件
	 * 
	 * @param filePath
	 *            文件路径（包含文件名）
	 * @param String
	 *            datas 文件内容
	 * 
	 * @return true or false
	 */
	public boolean writeFile(String filePath, String datas) {
		filePath = formatPath(filePath);
		return writeFile(filePath, datas, false, null);
	}

	/**
	 * 上传文件
	 * 
	 * @param filePath
	 *            文件路径（包含文件名）
	 * @param String
	 *            datas 文件内容
	 * @param auto
	 *            是否自动创建父级目录(最多10级)
	 * 
	 * @return true or false
	 */
	public boolean writeFile(String filePath, String datas, boolean auto) {
		filePath = formatPath(filePath);
		return writeFile(filePath, datas, auto, null);
	}

	/**
	 * 上传文件
	 * 
	 * @param filePath
	 *            文件路径（包含文件名）
	 * @param String
	 *            datas 文件内容
	 * @param auto
	 *            是否自动创建父级目录(最多10级)
	 * @param params
	 *            额外参数
	 * 
	 * @return true or false
	 */
	public boolean writeFile(String filePath, String datas, boolean auto,
			Map<String, String> params) {
		filePath = formatPath(filePath);
		boolean result = false;

		try {
			result = writeFile(filePath, datas.getBytes(Constants.UTF8), auto, params);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * 上传文件
	 * 
	 * @param filePath
	 *            文件路径（包含文件名）
	 * @param file
	 *            待上传的文件
	 * 
	 * @return true or false
	 * @throws IOException
	 */
	public boolean writeFile(String filePath, File file) throws IOException {
		filePath = formatPath(filePath);
		return writeFile(filePath, file, false, null);
	}

	/**
	 * 上传文件
	 * 
	 * @param filePath
	 *            文件路径（包含文件名）
	 * @param file
	 *            待上传的文件
	 * @param auto
	 *            是否自动创建父级目录(最多10级)
	 * 
	 * @return true or false
	 * @throws IOException
	 */
	public boolean writeFile(String filePath, File file, boolean auto)
			throws IOException {
		filePath = formatPath(filePath);
		return writeFile(filePath, file, auto, null);
	}
	
	/**
	 * 获取文件信息
	 * 
	 * @param filePath
	 *            文件路径（包含文件名）
	 * 
	 * @return 文件信息 或 null
	 */
	public Map<String, String> getFileInfo(String filePath) {

		HttpAction(Constants.METHOD_HEAD, Util.formatPath(this.bucketName, filePath));

		// 判断是否存在文件信息
		if (Util.isEmpty(fileType) && Util.isEmpty(fileSize) && Util.isEmpty(fileDate)) {
			return null;
		}

		Map<String, String> mp = new HashMap<String, String>();
		mp.put("type", fileType);
		mp.put("size", fileSize);
		mp.put("date", fileDate);

		return mp;
	}
	
	/**
	 * 获取总体空间的占用量
	 * 
	 * @param path
	 *            目标路径
	 * @return 空间占用量，失败时返回 -1
	 */
	public long getBucketUsage() {
		return getFolderUsage("/");
	}

	/**
	 * 获取某个子目录的占用量
	 * 
	 * @param path
	 *            目标路径
	 * @return 空间占用量，失败时返回 -1
	 */
	public long getFolderUsage(String path) {

		long usage = -1;

		String result = HttpAction(Constants.METHOD_GET, Util.formatPath(this.bucketName, path) + "/?usage");

		if (!Util.isEmpty(result)) {

			try {
				usage = Long.parseLong(result.trim());
			} catch (NumberFormatException e) {
			}
		}

		return usage;
	}

	/**
	 * 读取文件
	 * 
	 * @param filePath
	 *            文件路径（包含文件名）
	 * 
	 * @return 文件内容 或 null
	 */
	public String readFile(String filePath) {
		return HttpAction(Constants.METHOD_GET, Util.formatPath(this.bucketName, filePath));
	}

	/**
	 * 读取文件
	 * 
	 * @param filePath
	 *            文件路径（包含文件名）
	 * @param file
	 *            临时文件
	 * 
	 * @return true or false
	 */
	public boolean readFile(String filePath, File file) {

		String result = HttpAction(Constants.METHOD_GET, Util.formatPath(this.bucketName, filePath), null,
				file, false);

		return "".equals(result);
	}

	/**
	 * 删除文件
	 * 
	 * @param filePath
	 *            文件路径（包含文件名）
	 * 
	 * @return true or false
	 */
	public boolean deleteFile(String filePath) {

		return HttpAction(Constants.METHOD_DELETE, Util.formatPath(this.bucketName, filePath)) != null;
	}

//	/**
//	 * 获取上传文件后的信息（仅图片空间有返回数据）
//	 * 
//	 * @param key
//	 *            信息字段名（x-upyun-width、x-upyun-height、x-upyun-frames、x-upyun-file
//	 *            -type）
//	 * 
//	 * @return value or NULL
//	 * @deprecated
//	 */
//	public String getWritedFileInfo(String key) {
//
//		if (Util.isEmpty(picWidth))
//			return null;
//
//		if (Constants.X_UPYUN_WIDTH.equals(key))
//			return picWidth;
//		if (Constants.X_UPYUN_HEIGHT.equals(key))
//			return picHeight;
//		if (Constants.X_UPYUN_FRAMES.equals(key))
//			return picFrames;
//		if (Constants.X_UPYUN_FILE_TYPE.equals(key))
//			return picType;
//
//		return null;
//	}

	/**
	 * 连接处理逻辑
	 * 
	 * @param method
	 *            请求方式 {GET, POST, PUT, DELETE}
	 * @param uri
	 *            请求地址
	 * 
	 * @return 请求结果（字符串）或 null
	 */
	private String HttpAction(String method, String uri) {
		return HttpAction(method, uri, null, null, false);
	}

	/**
	 * 连接处理逻辑
	 * 
	 * @param method
	 *            请求方式 {GET, POST, PUT, DELETE}
	 * @param uri
	 *            请求地址
	 * @param datas
	 *            该请求所需发送数据（可为 null）
	 * @param outFile
	 *            文件描述符（可为 null）
	 * @param auto
	 *            自动创建父级目录(最多10级)
	 * 
	 * @return 请求结果（字符串）或 null
	 */
	private String HttpAction(String method, String uri, byte[] datas,
			File outFile, boolean auto) {

		return HttpAction(method, uri, datas, outFile, auto, null);
	}
}
