# 又拍云Java SDK

又拍云存储Java SDK，基于 [又拍云存储HTTP REST API接口](http://wiki.upyun.com/index.php?title=HTTP_REST_API%E6%8E%A5%E5%8F%A3) 开发。

## 更新说明
使用1.0.x系列版本SDK的用户，注意原有部分方法已经不再推荐使用，但是出于兼容考虑目前任然保留，建议更新升级程序使用新版SDK提供的方法。

## 使用说明

### 初始化UpYun
````
UpYun upyun = new UpYun("bucketName", "userName", "userPwd");
````

参数 `bucketName` 为空间名称，`userName`、`userPwd` 为授权操作员的账号密码。

根据国内的网络情况，又拍云存储API目前提供了电信、联通网通、移动铁通三个接入点，在upyun初始化后，通过setter方法进行设置，默认根据网络条件自动选择接入点。

````
upyun.setApiDomain(UpYun.ED_AUTO);
````

接入点有四个值可选：

* **UpYun.ED_AUTO** 根据网络条件自动选择接入点
* **UpYun.ED_TELECOM** 电信接入点
* **UpYun.ED_CNC** 联通网通接入点
* **UpYun.ED_CTT** 移动铁通接入点

默认参数为自动选择API接入点。但是我们推荐根据服务器网络状况，手动设置合理的接入点已获取最佳的访问速度。

### 上传文件

````
// 直接传递文件内容的形式上传
upyun.uploadFile("/temp/text_demo.txt", "Hello World", true);

// 数据流方式上传，可降低内存占用
File file = new File("/temp/text_demo.txt");
upyun.putFile(filePath, file, true);
````
第三个参数为可选。`True` 表示自动创建相应目录，默认值为`False`。

本方法还有一个Map类型的可选参数，用来设置文件类型、缩略图处理等参数。

````
Map<String, String> params = new HashMap<String, String>();
params.put(PARAMS.KEY_X_GMKERL_THUMBNAIL.getValue(), "small");
File file = new File("/temp/text_demo.txt");
upyun.writeFile(filePath, file, params, true);
````
该参数可以设置的值还包括：

* PARAMS.KEY_X_GMKERL_THUMBNAIL
* PARAMS.KEY_X_GMKERL_TYPE
* PARAMS.KEY_X_GMKERL_VALUE
* PARAMS.KEY_X_GMKERL_QUALITY
* PARAMS.KEY_X_GMKERL_UNSHARP
* PARAMS.KEY_X_GMKERL_CROP
* PARAMS.KEY_X_GMKERL_ROTATE

参数的具体使用方法，请参考 [标准API上传文件](http://wiki.upyun.com/index.php?title=%E6%A0%87%E5%87%86API%E4%B8%8A%E4%BC%A0%E6%96%87%E4%BB%B6)

空间上传成功后返回`True`，图片空间上传成功后可以通过以下方式获取图片信息

````
upyun.getPicWidth(); //图片宽度
upyun.getPicHeight();//图片高度
upyun.getPicFrames();//图片帧数
upyun.getPicType();  //图片类型
````
如果上传失败，则会抛出异常。

### 下载文件
````
// 直接读取文件内容
String data = upyun.readFile("/temp/upload_demo.txt");

// 使用数据流模式下载，节省内存占用
File file = File.createTempFile("upyunTempFile_", "");
boolean result = upyun.readFile(filePath, file);
````

直接获取文件时，返回文件内容，使用数据流形式获取时，成功返回`true`。
如果获取文件失败，则抛出异常。

### 创建目录
````
boolean result = upyun.mkDir("/a/b/c/", true);
````
目录路径必须以斜杠 `/` 结尾，创建成功返回 `true`，否则抛出异常。

### 删除目录或者文件
````
boolean result = upyun.rmDir("/demo/"); // 删除目录

boolean result = upyun.deleteFile("/demo/demo.png"); // 删除文件
````
删除成功返回 `true` ，否则抛出异常。
注意删除目录时，`必须保证目录为空` ，否则也会抛出异常。

### 获取目录文件列表
````
List<UpYun.FolderItem> items = upyun.readDir("/demo/");
````
需要获取根目录列表时，使用 `upyun.readDir("/")` 。
目录获取失败则抛出异常。

### 获取文件信息
````
Map<String, String> results = upyun.getFileInfo("/demo/demo.png");
results.get("type"); // 文件类型
results.get("size"); // 文件大小
results.get("date"); // 创建日期
````
返回结果为一个Map。

### 获取空间使用状况
````
long usage = upyun.getBucketUsage();	// 获取Bucket空间使用情况
long usage = upyun.getFolderUsage("/demo/");; 获取目录空间使用情况
````
返回的结果为空间使用量，单位 ***Byte***

## 异常处理
当API请求发生错误时，SDK将抛出异常，具体错误代码请参考 [标准API错误代码表](http://wiki.upyun.com/index.php?title=%E6%A0%87%E5%87%86API%E9%94%99%E8%AF%AF%E4%BB%A3%E7%A0%81%E8%A1%A8)
