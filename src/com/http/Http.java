package com.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import com.constant.Constants;
import com.constant.PARAMS;
import com.pojo.RequestParams;
import com.pojo.ResponseData;
import com.util.Util;

public class Http {
	
	private HttpURLConnection getConnection(String urlStr, String method, int timeout, boolean isCache, boolean isDoOutput) throws IOException{
		URL url = new URL(urlStr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		conn.setRequestMethod(method);
		conn.setConnectTimeout(timeout);
		conn.setUseCaches(isCache);
		conn.setDoOutput(isDoOutput);
		
		return conn;
	}
	
	private void closeConnection(HttpURLConnection conn){
		if (conn != null) {
			conn.disconnect();
			conn = null;
		}
	}
	/**
	 * 上传文件
	 * 
	 * @param filePath 保存在又拍云的文件路径（包含文件名）
	 * @param file     待上传的文件
	 * @param auto     是否自动创建父级目录(最多10级)
	 * @param params   额外参数
	 * 
	 * @return true or false
	 * @throws IOException
	 */
	
	//url:"http://" + apiDomain + filePath(包括文件名)
	public boolean writeFile(boolean isDebug, RequestParams requestParams, ResponseData responseData) throws IOException {

		InputStream is = null;
		OutputStream os = null;
		HttpURLConnection conn = null;

		try {
			conn = this.getConnection(requestParams.getRequestUrl(), Constants.METHOD_PUT, requestParams.getTimeout(), false, true);

			// 读取待上传的文件
			is = new FileInputStream(requestParams.getFile());

			String date = Util.getGMTDate();
			// 设置时间
			conn.setRequestProperty(Constants.DATE, date);
			// 设置签名
			conn.setRequestProperty(Constants.AUTHORIZATION,
					"UpYun " + requestParams.getUsername() + ":" + requestParams.getSignature());

			// 设置文件的 MD5 参数
			if (!Util.isEmpty(requestParams.getContentMD5())) {
				conn.setRequestProperty(Constants.CONTENT_MD5, requestParams.getContentMD5());
			}

			// 设置文件的访问密钥
			if (!Util.isEmpty(requestParams.getFileSecret())) {
				conn.setRequestProperty(Constants.CONTENT_SECRET, requestParams.getFileSecret());
			}

			// 是否自动创建父级目录
			if (requestParams.isAuto()) {
				conn.setRequestProperty(Constants.MKDIR, "true");
			}

			// 设置额外的参数，如图片缩略图等
			if (requestParams.getParams() != null && !requestParams.getParams().isEmpty()) {

				for (Map.Entry<String, String> param : requestParams.getParams().entrySet()) {
					conn.setRequestProperty(param.getKey(), param.getValue());
				}
			}

			// 创建链接
			conn.connect();

			os = conn.getOutputStream();
			byte[] data = new byte[4096];
			int temp = 0;

			// 上传文件内容
			while ((temp = is.read(data)) != -1) {
				os.write(data, 0, temp);
			}

			// 获取返回的信息
			getText(conn, false, responseData);

			// 上传成功
			return true;

		} catch (IOException e) {
			if (isDebug)
				e.printStackTrace();

			// 上传失败
			return false;

		} finally {

			if (os != null) {
				os.close();
				os = null;
			}
			if (is != null) {
				is.close();
				is = null;
			}
			closeConnection(conn);
		}
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
	 * @param params
	 *            额外参数
	 * 
	 * @return 请求结果（字符串）或 null
	 */
	
	//url ："http://" + apiDomain + uri
	public String HttpAction(boolean isDebug, RequestParams requestParams, ResponseData responseData) {

		String result = null;

		HttpURLConnection conn = null;
		OutputStream os = null;
		InputStream is = null;

		try {
			conn = this.getConnection(requestParams.getRequestUrl(), requestParams.getMethod(), requestParams.getTimeout(), false, true);
			//这段代码什么意思
//			if(method.equals(Constants.METHOD_DELETE)){
//				conn.setDoOutput(true);
//			}else{
//				conn.setDoOutput(true);
//			}

			// 设置时间
			conn.setRequestProperty(Constants.DATE, Util.getGMTDate());

			// 是否自动创建父级目录
			if (requestParams.isAuto()) {
				conn.setRequestProperty(Constants.MKDIR, "true");
			}

			long contentLength = 0;
			if (requestParams.getDatas() == null)
				conn.setRequestProperty(Constants.CONTENT_LENGTH, "0");
			else {
				contentLength = requestParams.getDatas().length;
				conn.setRequestProperty(Constants.CONTENT_LENGTH,
						String.valueOf(requestParams.getDatas().length));

				// 设置文件的 MD5 参数
				if (!Util.isEmpty(requestParams.getContentMD5())) {
					conn.setRequestProperty(Constants.CONTENT_MD5, requestParams.getContentMD5());
				}
				// 设置文件的访问密钥
				if (!Util.isEmpty(requestParams.getFileSecret())) {
					conn.setRequestProperty(Constants.CONTENT_SECRET, requestParams.getFileSecret());
				}
			}

			// 设置签名
			conn.setRequestProperty(Constants.AUTHORIZATION,
					requestParams.getSignature());

			// 是否是创建文件目录
			boolean isFolder = false;

			// 设置额外的参数，如图片缩略图等
			if (requestParams.getParams() != null && !requestParams.getParams().isEmpty()) {

				isFolder = !Util.isEmpty(requestParams.getParams().get(PARAMS.KEY_MAKE_DIR.getValue()));

				for (Map.Entry<String, String> param : requestParams.getParams().entrySet()) {
					conn.setRequestProperty(param.getKey(), param.getValue());
				}
			}

			// 创建链接
			conn.connect();

			if (requestParams.getDatas() != null) {
				os = conn.getOutputStream();
				os.write(requestParams.getDatas());
				os.flush();
			}

			if (isFolder) {
				os = conn.getOutputStream();
				os.flush();
			}

			if (!responseData.isReadFile()) {

				result = getText(conn, Constants.METHOD_HEAD.equals(requestParams.getMethod()), responseData);

			} else {
				result = "";

				os = new FileOutputStream(responseData.getFile());
				byte[] data = new byte[4096];
				int temp = 0;

				is = conn.getInputStream();

				while ((temp = is.read(data)) != -1) {
					os.write(data, 0, temp);
				}
			}
		} catch (IOException e) {
			if (isDebug)
				e.printStackTrace();

			// 操作失败
			return null;

		} finally {

			try {
				if (os != null) {
					os.close();
					os = null;
				}
				if (is != null) {
					is.close();
					is = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.closeConnection(conn);
		}

		return result;
	}

	/**
	 * 获得连接请求的返回数据
	 * 
	 * @param conn
	 * 
	 * @return 字符串
	 */
	private String getText(HttpURLConnection conn, boolean isHeadMethod, ResponseData responseData)
			throws IOException {

		StringBuilder text = new StringBuilder();

		InputStream is = null;
		InputStreamReader sr = null;
		BufferedReader br = null;

		int code = conn.getResponseCode();

		try {
			is = code >= 400 ? conn.getErrorStream() : conn.getInputStream();

			if (!isHeadMethod) {
				sr = new InputStreamReader(is);
				br = new BufferedReader(sr);

				char[] chars = new char[4096];
				int length = 0;

				while ((length = br.read(chars)) != -1) {
					text.append(chars, 0, length);
				}
			}
			if (200 == code && conn.getHeaderField(Constants.X_UPYUN_WIDTH) != null) {
				responseData.setPicWidth(conn.getHeaderField(Constants.X_UPYUN_WIDTH));
				responseData.setPicHeight(conn.getHeaderField(Constants.X_UPYUN_HEIGHT));
				responseData.setPicFrames(conn.getHeaderField(Constants.X_UPYUN_FRAMES));
				responseData.setPicType(conn.getHeaderField(Constants.X_UPYUN_FILE_TYPE));
			}

			if (200 == code && conn.getHeaderField(Constants.X_UPYUN_FILE_TYPE) != null) {
				responseData.setFileType(conn.getHeaderField(Constants.X_UPYUN_FILE_TYPE));
				responseData.setFileSize(conn.getHeaderField(Constants.X_UPYUN_FILE_SIZE));
				responseData.setFileSize(conn.getHeaderField(Constants.X_UPYUN_FILE_DATE));
			}
		} finally {
			if (br != null) {
				br.close();
				br = null;
			}
			if (sr != null) {
				sr.close();
				sr = null;
			}
			if (is != null) {
				is.close();
				is = null;
			}
		}

		if (isHeadMethod) {
			if (code >= 400)
				return null;
			return "";
		}

		if (code >= 400)
			throw new IOException(text.toString());

		return text.toString();
	}
}
