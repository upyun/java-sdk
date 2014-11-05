package com.pojo;

import java.util.Date;

public class FolderItem {
	// 文件名
	public String name;

	// 文件类型 {file, folder}
	public String type;

	// 文件大小
	public long size;

	// 文件日期
	public Date date;

	public FolderItem(String data) {
		String[] a = data.split("\t");
		if (a.length == 4) {
			this.name = a[0];
			this.type = ("N".equals(a[1]) ? "File" : "Folder");
			try {
				this.size = Long.parseLong(a[2].trim());
			} catch (NumberFormatException e) {
				this.size = -1;
			}
			long da = 0;
			try {
				da = Long.parseLong(a[3].trim());
			} catch (NumberFormatException e) {
			}
			this.date = new Date(da * 1000);
		}
	}

	@Override
	public String toString() {
		return "time = " + date + "  size = " + size + "  type = " + type
				+ "  name = " + name;
	}
}