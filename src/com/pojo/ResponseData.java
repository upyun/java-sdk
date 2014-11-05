package com.pojo;

import java.io.File;

public class ResponseData {
	private boolean isReadFile;
	private boolean isReadHead;
	
	private File file = null;
	
	//图片返回参数
	private String picWidth = null;
	private String picHeight = null;
	private String picFrames = null;
	private String picType = null;
	
	//文件类型返回参数
	private String fileType = null;
	private String fileSize = null;
	private String fileDate = null;
	
	public boolean isReadFile() {
		return isReadFile;
	}
	public void setReadFile(boolean isReadFile) {
		this.isReadFile = isReadFile;
	}
	public boolean isReadHead() {
		return isReadHead;
	}
	public void setReadHead(boolean isReadHead) {
		this.isReadHead = isReadHead;
	}
	public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}
	public String getPicWidth() {
		return picWidth;
	}
	public void setPicWidth(String picWidth) {
		this.picWidth = picWidth;
	}
	public String getPicHeight() {
		return picHeight;
	}
	public void setPicHeight(String picHeight) {
		this.picHeight = picHeight;
	}
	public String getPicFrames() {
		return picFrames;
	}
	public void setPicFrames(String picFrames) {
		this.picFrames = picFrames;
	}
	public String getPicType() {
		return picType;
	}
	public void setPicType(String picType) {
		this.picType = picType;
	}
	public String getFileType() {
		return fileType;
	}
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	public String getFileSize() {
		return fileSize;
	}
	public void setFileSize(String fileSize) {
		this.fileSize = fileSize;
	}
	public String getFileDate() {
		return fileDate;
	}
	public void setFileDate(String fileDate) {
		this.fileDate = fileDate;
	}
	
}
