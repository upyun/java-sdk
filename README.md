# UPYUN Java SDK

[![Build Status](https://travis-ci.org/upyun/java-sdk.svg?branch=master)](https://travis-ci.org/upyun/java-sdk)

又拍云存储 Java SDK，基于 [又拍云存储 HTTP REST API 接口](http://docs.upyun.com/api/rest_api/) ， [又拍云 HTTP FORM API 接口](http://docs.upyun.com/api/form_api/) 和 [又拍云云处理文档 ](http://docs.upyun.com/cloud/)开发，适用于 Java 7 及以上版本。

## Maven 安装

```
<dependency>
  <groupId>com.upyun</groupId>
  <artifactId>java-sdk</artifactId>
  <version>3.14</version>
</dependency>

```

**更新说明**

使用1.0.x系列版本 SDK 的用户，注意原有部分方法已经不再推荐使用，但是出于兼容考虑目前任然保留，建议更新升级程序使用新版 SDK 提供的方法。

## 目录
* [云存储基础接口](#云存储基础接口)
  * [初始化 UpYun](#初始化 UpYun)
  * [创建目录](#创建目录)
  * [删除目录](#删除目录)
  * [获取目录文件列表](#获取目录文件列表)
  * [上传文件](#上传文件)
  * [获取文件信息](#获取文件信息)
  * [获取使用量信息](#获取使用量信息)
  * [下载文件](#下载文件)
  * [删除文件](#删除文件)
  * [断点续传](#断点续传)
* [图片处理接口](#图片处理接口)
  * [制作图片缩略图](#制作图片缩略图)
  * [图片裁剪](#图片裁剪)
  * [图片旋转](#图片旋转)
* [表单上传接口](#表单上传接口)
  * [初始化 FormUploader](#初始化 FormUploader)
  * [表单上传文件](#表单上传文件)
  * [表单上传作图](#表单上传作图)
  * [错误说明](#错误说明)
* [云处理](#云处理)
  * [异步音视频处理](#异步音视频处理)
     * [初始化 MediaHandler](#初始化 MediaHandler)
  	  * [发起异步处理请求](#发起异步处理请求)
  	  * [查询处理进度](#查询处理进度)
  	  * [查询处理结果](#查询处理结果)
  * [压缩解压缩](#压缩解压缩)
     * [初始化 CompressHandler](#初始化 CompressHandler)
     * [发起异步处理请求](#发起异步处理请求)
  * [异步文件拉取](#异步文件拉取)
     * [初始化 PullingHandler](#初始化 PullingHandler)
     * [发起异步处理请求](#发起异步处理请求)

<a name="云存储基础接口"></a>
## 云存储基础接口

<a name="初始化 UpYun"></a>
### 初始化 UpYun

```Java
    UpYun upyun = new UpYun("空间名称", "操作员名称", "操作员密码");
```

**可选属性：**

- 是否开启 debug 模式：默认不开启

```Java
    upyun.setDebug(true);
```

- 手动设置超时时间：默认为30秒

```Java
    upyun.setTimeout(60);
```

- 选择最优的接入点

```Java
    upyun.setApiDomain(UpYun.ED_AUTO);
```
>根据国内的网络情况，又拍云存储 API 目前提供了电信、联通网通、移动铁通三个接入点。可以通过`setApiDomain()`方法进行设置，默认将根据网络条件自动选择接入点。

> 接入点有四个值可选：

```Java
	UpYun.ED_AUTO    //根据网络条件自动选择接入点
	UpYun.ED_TELECOM //电信接入点
	UpYun.ED_CNC     //联通网通接入点
	UpYun.ED_CTT     //移动铁通接入点
```

_**注：**建议大家根据服务器网络状况，手动设置合理的接入点已获取最佳的访问速度_

---

<a name="创建目录"></a>
### 创建目录

**方法原型：**

```Java
	public boolean mkDir(String path, boolean auto);
```
**参数说明：**

* `path`	目录路径，以`/`结尾
* `auto`	（可选）：若为 `true` 则自动创建父级目录（只支持自动创建10级以内的父级目录）

**返回值说明：**

* 结果为 `true` 创建目录成功
* 若空间相同目录下已经存在同名的文件，则将返回『不允许创建目录』的错误


**举例说明：**

```Java
	String path = "/dir1/dir2/";
    // 创建目录，自动创建父级目录
    boolean result = upyun.mkDir(path, true);
```

---

<a name="删除目录"></a>
### 删除目录

**方法原型：**

```Java
public boolean rmDir(String path);
```
**参数说明：**

* `path`	目录路径

**返回值说明：**

* 结果为 `true` 删除目录成功
* 若待删除的目录 `path` 下还存在任何文件或子目录，将返回『不允许删除』的错误

**举例说明：**

```Java
	String path = "/dir1/dir2/";
    // 删除目录
    boolean result = upyun.rmDir(path); 
```

---

<a name="获取目录文件列表"></a>
### 获取目录文件列表

**方法原型：**

```Java
public List<FolderItem> readDir(String path);
```
>`UpYun.FolderItem` 包含属性:
>
>* `name`  文件名
>* `type`  文件类型
>* `size`  文件大小
>* `date`  文件创建日期
>
>以上属性作用域皆为 `public`，可直接调用

**参数说明：**

* `path`  目录路径

**返回值说明：**

* 若 `path` 目录没有内容时，返回`null`
* 若 `path` 目录不存在时，则将返『不存在目录』的错误

**举例说明：**

```Java   
	String path = "/dir1/";
    // 获取目录中文件列表
    List<UpYun.FolderItem> items = upyun.readDir(path);
    for (int i = 0; i < items.size(); i++) {
		System.out.println(items.get(i));
	}
```

---

<a name="上传文件"></a>
### 上传文件

**方法原型：**

```Java
public boolean writeFile(String filePath, String datas, boolean auto);
public boolean writeFile(String filePath, File file, boolean auto);
public boolean writeFile(String filePath, byte[] datas, boolean auto);
```
**参数说明：**

* `filePath`  保存到又拍云存储的文件路径，以`/`开始
* 第二个参数  接受 `String` 、 `File` 和 `byte[]` 三种类型的数据
* `auto`  （可选）：若为 `true` 则自动创建父级目录（只支持自动创建10级以内的父级目录）

**返回值说明：**

* 结果为 `true` 上传文件成功

**可选属性：**

* 上传文件时可进行文件的 `MD5` 校验，若又拍云服务端收到的文件MD5值与用户设置的不一致，将返回 `406 Not Acceptable` 错误。对于需要确保上传文件的完整性要求的业务，可以设置该参数：

```Java
    upyun.setContentMD5(UpYun.md5(file));  
```

**举例说明：**

```Java
    // 例1：上传纯文本内容，自动创建父级目录
    String str = "Hello UpYun";
    boolean result = upyun.writeFile("/path/to/file", str, true);

    // 例2：采用数据流模式上传文件（节省内存）,自动创建父级目录
	File file = new File(localFilePath);
	upyun.setContentMD5(UpYun.md5(file));
	boolean result = upyun.writeFile(filePath, file, true);
```

_**注：**
若空间内指定目录已存在相同文件，则会被覆盖，且**不可逆**。若要避免此情况，可以先通过[获取文件信息](#获取文件信息)来判断是否已经存在相同文件_


---

<a name="获取文件信息"></a>
### 获取文件信息

**方法原型：**

```Java
public Map<String, String> getFileInfo(String filePath);
```

**参数说明：**

* `filePath`  又拍云中文件的路径

**返回值说明：**

* 若 `filePath` 所指定文件不存在，则直接返回 `null`
* `Map` 包含3个Key：

> * `type`  文件类型
> * `size`  文件大小
> * `date`  创建日期

**举例说明：**

```Java
	String filePath = "/path/to/file";
    // 获取文件信息
    Map<String, String> info = upyun.getFileInfo(filePath);
    String type = info.get("type"); 
    String size = info.get("size"); 
    String date = info.get("date");
```

---

<a name="获取使用量信息"></a>
### 获取使用量信息

**方法原型：**

```Java
	public long getBucketUsage();
	public long getFolderUsage(String path);
```

**举例说明：**

```Java
    // 例1：获取整个空间的使用量情况
    long usage = upyun.getBucketUsage();
    
    // 例2：获取某个目录的使用量情况
    long usage = upyun.getFolderUsage(dir);
```

**返回值说明：**

* 返回值单位为 Byte

---

<a name="下载文件"></a>
### 下载文件

**方法原型：**

```Java
	public String readFile(String filePath)；
	public boolean readFile(String filePath, File file)；
```

**参数说明：**

* `filePath`  文件在又拍云存储中的路径
* `file`  本地临时文件（用来保存下载下来的数据）

**返回值说明：**

* 方法一：文本内容
* 方法二：结果为 `true` 下载成功

**举例说明：**

```Java
    // 例1：直接读取文本内容
    String remoteFilePath = "/path/to/file";
    String datas = upyun.readFile(remoteFilePath);

    // 例2：下载文件，采用数据流模式下载文件（节省内存）
    String remoteFilePath = "/path/to/file";
    File file = new File(localFilePath); // 创建一个本地临时文件
    boolean result = upyun.readFile(remoteFilePath, file);
```

---

<a name="删除文件"></a>
### 删除文件

**方法原型：**

```Java
	public boolean deleteFile(String filePath);
```

**参数说明：**

* `filePath`  文件在又拍云的路径

**返回值说明：**

* 若 `filePath` 指定的文件不存在，则返回『文件不存在』的错误
* 结果为 `true` 删除文件成功

**举例说明：**

```Java
	String filePath = "/path/to/file";
    // 删除文件
    boolean result = upyun.deleteFile(filePath);
```

<a name="断点续传"></a>
### 断点续传
初始化 UpResume
```java
	UpResume resume = new UpResume("空间名称", "操作员名称", "操作员密码")
```
设置上传进度监听
```java
	 resume.setOnProgressListener(new UpResume.OnProgressListener()
```
设置 MD5 校验

```java
	resume.setCheckMD5(true);
```
开始上传

```java
	public boolean upload(String filePath, String uploadPath,Map<String, String> params)	
```

**参数说明：**

* `filePath ` 待上传文件路径
*  `uploadPath ` 上传至空间目录
*  `params `	通用可选上传参数见文档 可为null

**详细示例：** 
见 [ResumeUploadDemo](https://github.com/upyun/java-sdk/blob/master/src/main/java/demo/ResumeUploadDemo)

<a name="图片处理接口"></a>
## 图片处理接口


**方法原型：**

```Java
	public boolean writeFile(String filePath, File file, boolean 	auto, Map<String, String> params);
	public boolean writeFile(String filePath, byte[] datas, boolean 	auto, Map<String, String> params);
	public boolean writeFile(String filePath, String datas, boolean 	auto, Map<String, String> params);
```

**参数说明：**

* `filePath`  保存到又拍云存储的路径
* 第二个参数  接受 `String` 、`File` 和 `byte[]` 三种类型的**图片数据内容**
* `auto`  （可选）：自动创建父级目录（只支持自动创建10级以内的父级目录）
* `params`  自定义图片处理参数的组合，详情请看[params参数说明](http://docs.upyun.com/api/rest_api/#_4)

**返回值说明：**

* 结果为 `true` 图片上传并处理成功


图片处理包括『制作图片缩略图』，『图片裁剪』，『图片旋转』。只需要选则不同的 PARAMS 参数就可以分别完成这些操作，下面分别举例说明。

---

<a name="制作图片缩略图"></a>
### 制作图片缩略图

**举例说明：**

```Java
    Map<String, String> params = new HashMap<String, String>();
    
    // 设置缩略图类型
    params.put(PARAMS.KEY_X_GMKERL_TYPE.getValue(), PARAMS.VALUE_FIX_BOTH.getValue());
    
    // 设置缩略图参数值
    params.put(PARAMS.KEY_X_GMKERL_VALUE.getValue(), "150x150");
    
    // 设置缩略图的质量，默认 95
    params.put(PARAMS.KEY_X_GMKERL_QUALITY.getValue(), "95");
    
    // 待上传的图片文件
    File file = new File(localFilePath);
    
    String filePath = "/path/to/file";
    
    // 上传图片，并同时进行图片处理
    boolean result = upyun.writeFile(filePath, file, true, params);
```

---

<a name="图片裁剪"></a>
### 图片裁剪

**举例说明：**

```Java 
    // 设置缩略图的参数
    Map<String, String> params = new HashMap<String, String>();
    
    // 设置图片裁剪，参数格式：x,y,width,height
    params.put(PARAMS.KEY_X_GMKERL_CROP.getValue(), "0,0,100,100");
    
    // 待上传的图片文件
    File file = new File(localFilePath);
    
    // 上传图片，并同时进行图片处理
    boolean result = upyun.writeFile(savePath, file, autoMkDir, params);
```

---

<a name="图片旋转"></a>
### 图片旋转

**举例说明：**

```Java
   // 设置缩略图的参数
    Map<String, String> params = new HashMap<String, String>();
    
    // 设置图片旋转
    params.put(PARAMS.KEY_X_GMKERL_ROTATE.getValue(), PARAMS.VALUE_ROTATE_90.getValue());
    
    // 待上传的图片文件
    File file = new File(localFilePath);
    
    // 上传图片，并同时进行图片处理
    boolean result = upyun.writeFile(savePath, file, autoMkDir, params);
```

<a name="表单上传接口"></a>
## 表单上传接口

<a name="初始化 FormUploader"></a>
### 初始化 FormUploader

```java
 	public FormUploader(String bucketName, String userName, String password) 
```
参数说明：

* `bucketName `	 空间名
* `userName `  操作员
* `password `  密码

**可选属性：**

- 手动设置超时时间：默认为30秒

```java
	public void setTimeout(int timeout)
```

- 选择最优的接入点,默认 `v0.api.upyun.com`

```Java
    public void setApiDomain(String domain)
```

- 选择默认过期时间，默认1800秒

```java
	public void setExpiration(int expiration)
```

<a name="表单上传文件"></a>
### 表单上传文件

**方法原型：**

```java
	public Result upload(Map<String, Object> params, File file) 
	public Result upload(Map<String, Object> params, byte[] datas) 
```
**参数说明：**

* `params `  参数键值对
* `file `  上传文件
* `datas ` 上传数组

参数键值对中 `Params.SAVE_KEY` 为必选参数，其他可选参数见 [Params](https://github.com/upyun/java-sdk/blob/master/src/main/java/com/upyun/Params.java) 或者[官网 API 文档](http://docs.upyun.com/api/form_api/#api_1)。

**返回说明:**

> * `Result.Succeed`  是否成功
> * `Result.code`  返回http消息码
> * `Result.msg`  返回消息

**举例说明：**

表单上传示例可见[ FormUploadDemo ](https://github.com/upyun/java-sdk/blob/master/src/main/java/demo/FormUploadDemo.java)。

<a name="表单上传作图"></a>
###表单上传作图

**上传同步作图**

```java
     private static void testSync() {
        //初始化uploader
        FormUploader uploader = new FormUploader(BUCKET_NAME, APIKEY, null);

        //初始化参数组 Map
        final Map<String, Object> paramsMap = new HashMap<String, Object>();

        //添加 SAVE_KEY 参数
        paramsMap.put(Params.SAVE_KEY, savePath);

        //添加同步上传作图参数 X_GMKERL_THUMB
        paramsMap.put(Params.X_GMKERL_THUMB, "/fw/300/unsharp/true/quality/80/format/png");

        //打印结果
        System.out.println(uploader.upload(paramsMap, file));
    }	
```

`paramsMap` 添加键值对，`Params.X_GMKERL_THUMB` 为 key，作图规则见[上传作图 API](http://docs.upyun.com/cloud/image/#_2)

**上传异步作图**

```java
	 private static void testAsync() {
        //uploader
        FormUploader uploader = new FormUploader(BUCKET_NAME, APIKEY, null);

        //初始化参数组 Map
        final Map<String, Object> paramsMap = new HashMap<String, Object>();

        //添加 SAVE_KEY 参数
        paramsMap.put(Params.SAVE_KEY, savePath);

        //初始化JSONArray
        JSONArray array = new JSONArray();

        //初始化JSONObject
        JSONObject json = new JSONObject();

        //json 添加 name 属性
        json.put("name", "thumb");

        //json 添加 X_GMKERL_THUMB 属性
        json.put(Params.X_GMKERL_THUMB, "/fw/300/unsharp/true/quality/80/format/png");

        //json 添加 save_as 属性
        json.put("save_as", "/path/to/fw_100.jpg");

        //json 添加 notify_url 属性
        json.put("notify_url","http://httpbin.org/post");

        //将json 对象放入 JSONArray
        array.put(json);

        //添加异步作图参数 APPS
        paramsMap.put(Params.APPS, array);

        //打印结果
        System.out.println(uploader.upload(paramsMap, file));
    }
```
`paramsMap` 添加键值对，`Params.APPS` 为 key，作图规则见[上传作图 API](http://docs.upyun.com/cloud/image/#_2)

<a name="云处理"></a>
##云处理

<a name="异步音视频处理"></a>
### 异步音视频处理

<a name="初始化 MediaHandler"></a>
#### 初始化 MediaHandler
```java
	MediaHandler handle = new MediaHandler(BUCKET_NAME, OPERATOR_NAME, OPERATOR_PWD);
```
参数说明：

* `BUCKET_NAME `	 空间名
* `OPERATOR_NAME `  操作员名称
* `OPERATOR_PWD `  操作员密码

**可选属性：**

- 手动设置超时时间：默认为30秒

```java
	public void setTimeout(int timeout)
```

<a name="发起异步处理请求"></a>
### 发起异步处理请求

**方法原型：**

```java
	 public Result process(Map<String, Object> params) throws IOException 
```
**参数说明：**

* `params `  参数键值对

详细参数可见 [MediaHandler](https://github.com/upyun/java-sdk/blob/master/src/main/java/com/upyun/MediaHandler.java) 或者[官网 API 文档](http://docs.upyun.com/cloud/av/#_3)。

**返回说明:**

> * `Result.Succeed`  是否成功
> * `Result.code`  返回http消息码
> * `Result.msg`  返回消息

**举例说明：**

示例可见[ testMediaProcess ](https://github.com/upyun/java-sdk/blob/master/src/main/java/demo/MediaHandlerDemo.java)。


<a name="查询处理进度"></a>
### 查询处理进度
**方法原型：**

```java
	 public Result getStatus(Map<String, Object> params) throws IOException 
```
**参数说明：**

* `params `  参数键值对 包括 `bucket_name` 和 `task_ids `

**返回说明:**

同上

**举例说明：**

示例可见[ testMediaStatus ](https://github.com/upyun/java-sdk/blob/master/src/main/java/demo/MediaHandlerDemo.java)。

<a name="查询处理结果"></a>
### 查询处理结果
**方法原型：**

```java
	 public Result getResult(Map<String, Object> params) throws IOException
```
**参数说明：**

同上

**返回说明:**

同上

**举例说明：**

示例可见[ testMediaResult ](https://github.com/upyun/java-sdk/blob/master/src/main/java/demo/MediaHandlerDemo.java)。

<a name="压缩解压缩"></a>
###压缩解压缩

<a name="初始化 CompressHandler"></a>
####初始化 CompressHandler

```java
	MediaHandler handle = new MediaHandler(BUCKET_NAME, OPERATOR_NAME, OPERATOR_PWD);
```
参数说明：

* `BUCKET_NAME `	 空间名
* `OPERATOR_NAME `  操作员名称
* `OPERATOR_PWD `  操作员密码


<a name="发起异步处理请求"></a>
####发起异步处理请求

```java
	 public Result process(Map<String, Object> params) throws IOException 
```
**参数说明：**

* `params `  参数键值对

详细参数可见 [CompressHandler](https://github.com/upyun/java-sdk/blob/master/src/main/java/com/upyun/CompressHandler.java) 或者[官网 API 文档](http://docs.upyun.com/cloud/unzip/)。

详细示例见[CompressDemo](https://github.com/upyun/java-sdk/blob/master/src/main/java/demo/CompressDemo.java)

<a name="异步文件拉取"></a>
###异步文件拉取

<a name="初始化 CompressHandler"></a>
####初始化 CompressHandler

```java
	MediaHandler handle = new MediaHandler(BUCKET_NAME, OPERATOR_NAME, OPERATOR_PWD);
```
参数说明：同上

<a name="发起异步处理请求"></a>
####发起异步处理请求

```java
	 public Result process(Map<String, Object> params) throws IOException 
```
**参数说明：**

* `params `  参数键值对

详细参数可见 [PullingHandler](https://github.com/upyun/java-sdk/blob/master/src/main/java/com/upyun/PullingHandler.java) 或者[官网 API 文档](http://docs.upyun.com/cloud/spider/)。

详细示例见[PullingDemo](https://github.com/upyun/java-sdk/blob/master/src/main/java/demo/PullingDemo.java)


<a name="错误说明"></a>
##错误说明

请参照 [API 错误码表](http://docs.upyun.com/api/errno/#api)

