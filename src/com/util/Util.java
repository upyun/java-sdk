package com.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.constant.Constants;

public class Util {
	
	/**
	 * 计算签名
	 * 
	 * @param conn
	 *            连接
	 * @param uri
	 *            请求地址
	 * @param length
	 *            请求所发Body数据长度
	 * 
	 * @return 签名字符串
	 * 
	 */
	//这里的date可能会导致签名不成功 conn.getRequestProperty(Constants.DATE)
	private String sign(String method, String uri, String date, long length, String password) {
		String sign = method + "&" + uri + "&" + date + "&" + length + "&" + password;
		return Util.md5(sign);
	}
	
	/**
	 * 格式化路径参数，去除前后的空格并确保以"/"开头，最后添加"/空间名"
	 * <p>
	 * 最终构成的格式："/空间名/文件路径"
	 * 
	 * @param path
	 *            目录路径或文件路径
	 * @return 格式化后的路径
	 */
	public static String formatPath(String bucketName, String path) {

		if (!Util.isEmpty(path)) {

			// 去除前后的空格
			path = path.trim();

			// 确保路径以"/"开头
			if (!path.startsWith(Constants.SEPARATOR)) {
				return Constants.SEPARATOR + bucketName + Constants.SEPARATOR + path;
			}
		}

		return Constants.SEPARATOR + bucketName + path;
	}
	
	/**
	 * 获取 GMT 格式时间戳
	 * 
	 * @return GMT 格式时间戳
	 */
	public static String getGMTDate() {
		SimpleDateFormat formater = new SimpleDateFormat(
				"EEE, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
		formater.setTimeZone(TimeZone.getTimeZone("GMT"));
		return formater.format(new Date());
	}
	
	/**
	 * 判断字符串是否为空
	 * 
	 * @param str
	 * @return 是否为空
	 */
	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}
	
	/**
	 * 对字符串进行 MD5 加密
	 * 
	 * @param str
	 *            待加密字符串
	 * 
	 * @return 加密后字符串
	 */
	public static String md5(String str) {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
			md5.update(str.getBytes(Constants.UTF8));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
		byte[] encodedValue = md5.digest();
		int j = encodedValue.length;
		char finalValue[] = new char[j * 2];
		int k = 0;
		for (int i = 0; i < j; i++) {
			byte encoded = encodedValue[i];
			finalValue[k++] = hexDigits[encoded >> 4 & 0xf];
			finalValue[k++] = hexDigits[encoded & 0xf];
		}

		return new String(finalValue);
	}
	

	/**
	 * 对文件进行 MD5 加密
	 * 
	 * @param file
	 *            待加密的文件
	 * 
	 * @return 文件加密后的 MD5 值
	 * @throws IOException
	 */
	public static String md5(File file) throws IOException {
		FileInputStream is = new FileInputStream(file);
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
			int n = 0;
			byte[] buffer = new byte[1024];
			do {
				n = is.read(buffer);
				if (n > 0) {
					md5.update(buffer, 0, n);
				}
			} while (n != -1);
			is.skip(0);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		} finally {
			is.close();
		}

		byte[] encodedValue = md5.digest();

		int j = encodedValue.length;
		char finalValue[] = new char[j * 2];
		int k = 0;
		for (int i = 0; i < j; i++) {
			byte encoded = encodedValue[i];
			finalValue[k++] = hexDigits[encoded >> 4 & 0xf];
			finalValue[k++] = hexDigits[encoded & 0xf];
		}

		return new String(finalValue);
	}
}
