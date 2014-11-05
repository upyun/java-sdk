package com.pojo;

import java.io.File;
import java.util.Map;
//String requestUrl, int timeout, File file, boolean auto,String signature, String userName, 
//String fileSecret, String contentMD5, Map<String, String> params
public class RequestParams {
	private String requestUrl;
	private int timeout;
	private File file = null;
	private boolean auto;
	private String signature;
	private String username;
	private String fileSecret;
	private String contentMD5;
	private Map<String,String> params = null;
	private byte[] datas = null;
	private String method;
	
	public byte[] getDatas() {
		return datas;
	}
	public void setDatas(byte[] datas) {
		this.datas = datas;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public String getRequestUrl() {
		return requestUrl;
	}
	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
	}
	public int getTimeout() {
		return timeout;
	}
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}
	public boolean isAuto() {
		return auto;
	}
	public void setAuto(boolean auto) {
		this.auto = auto;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getFileSecret() {
		return fileSecret;
	}
	public void setFileSecret(String fileSecret) {
		this.fileSecret = fileSecret;
	}
	public String getContentMD5() {
		return contentMD5;
	}
	public void setContentMD5(String contentMD5) {
		this.contentMD5 = contentMD5;
	}
	public Map<String, String> getParams() {
		return params;
	}
	public void setParams(Map<String, String> params) {
		this.params = params;
	}
}
