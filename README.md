# UPYUN Java SDK

[![Build Status](https://travis-ci.org/upyun/java-sdk.svg?branch=master)](https://travis-ci.org/upyun/java-sdk)

又拍云存储 Java SDK，基于 [又拍云存储 HTTP REST API 接口](http://docs.upyun.com/api/rest_api/) ， [又拍云 HTTP FORM API 接口](http://docs.upyun.com/api/form_api/) 和 [又拍云云处理文档 ](http://docs.upyun.com/cloud/)开发，适用于 Java 7 及以上版本。

## Maven 安装

```
<dependency>
  <groupId>com.upyun</groupId>
  <artifactId>java-sdk</artifactId>
  <version>4.2.3</version>
</dependency>

```

## 目录
* [云存储基础接口](#云存储基础接口)
  * [初始化 RestManager](#初始化RestManager)
  * [创建目录](#创建目录)
  * [删除目录](#删除目录)
  * [获取目录文件列表](#获取目录文件列表)
  * [上传文件](#上传文件)
  * [获取文件信息](#获取文件信息)
  * [获取使用量信息](#获取使用量信息)
  * [下载文件](#下载文件)
  * [删除文件](#删除文件)
  * [移动文件](#移动文件)
  * [复制文件](#复制文件)
  * [串行式断点续传](#串行式断点续传)
  * [并行式断点续传](#并行式断点续传)
* [表单上传接口](#表单上传接口)
  * [初始化 FormUploader](#初始化FormUploader)
  * [表单上传文件](#表单上传文件)
  * [表单上传作图](#表单上传作图)
  * [错误说明](#错误说明)
* [云处理](#云处理)
  * [异步音视频处理](#异步音视频处理)
  * [查询处理进度](#查询处理进度)
  * [查询处理结果](#查询处理结果)
  * [压缩解压缩](#压缩解压缩)
  * [异步文件拉取](#异步文件拉取)
  * [文档转换](#文档转换)
  * [图片拼接](#图片拼接)


<a name="云存储基础接口"></a>
## 云存储基础接口

<a name="初始化RestManager"></a>
### 初始化 RestManager

```Java
	RestManager manager = new RestManager("空间名称", "操作员名称", "操作员密码");
```

**可选属性：**

- 设置代理

```Java
    manager.setProxy(proxy);
```

- 手动设置超时时间：默认为30秒

```Java
    manager.setTimeout(60);
```

- 选择最优的接入点

```Java
    manager.setApiDomain(RestManager.ED_AUTO);
```
>根据国内的网络情况，又拍云存储 API 目前提供了电信、联通网通、移动铁通三个接入点。可以通过`setApiDomain()`方法进行设置，默认将根据网络条件自动选择接入点。

> 接入点有四个值可选：

```Java
	RestManager.ED_AUTO    //根据网络条件自动选择接入点
	RestManager.ED_TELECOM //电信接入点
	RestManager.ED_CNC     //联通网通接入点
	RestManager.ED_CTT     //移动铁通接入点
```

_**注：**建议大家根据服务器网络状况，手动设置合理的接入点已获取最佳的访问速度_

---

<a name="创建目录"></a>
### 创建目录

**方法原型：**

```Java
	public Response mkDir(String path);
```
**参数说明：**

* `path`	目录路径，以`/`结尾

**返回值说明：**

* 返回 Response 

**举例说明：**

```Java
	String path = "/dir1/dir2/";
    // 创建目录，自动创建父级目录
    Response result = manager.mkDir(path);
```

---

<a name="删除目录"></a>
### 删除目录

**方法原型：**

```Java
	public Response rmDir(String path);
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
    Response result = manager.rmDir(path); 
```

---

<a name="获取目录文件列表"></a>
### 获取目录文件列表

**方法原型：**

```Java
	public Response readDirIter(String path,Map<String, String> params);
```

**参数说明：**

* `path`  目录路径
* `params` 可选参数

**举例说明：**

```Java   
	String path = "/dir1/";
    // 获取目录中文件列表
    Response response = manager.readDirIter(path,null);
    System.out.println(response.body().string());
```

---

<a name="上传文件"></a>
### 上传文件

**方法原型：**

```Java
public Response writeFile(String filePath, byte[] data, Map<String, String> params)
public Response writeFile(String filePath, File file, Map<String, String> params)
public Response writeFile(String filePath, InputStream inputStream, Map<String, String> params)

```
**参数说明：**

* `filePath`  保存到又拍云存储的文件路径，以`/`开始
* 第二个参数  接受 `InputStream ` 、 `File` 和 `byte[]` 三种类型的数据
* params 上传额外可选参数，[详见 api 文档](https://help.upyun.com/knowledge-base/rest_api/#e4b88ae4bca0e69687e4bbb6)。

**返回值说明：**

* response.isSuccessful() 结果为 `true` 上传文件成功

**举例说明：**

```Java
    // 例1：上传纯文本内容，自动创建父级目录
    String str = "Hello RestManager";
    Map<String, String> params = new HashMap<String, String>();
        // 设置待上传文件的 Content-MD5 值
        // 如果又拍云服务端收到的文件MD5值与用户设置的不一致，将回报 406 NotAcceptable 错误
    params.put(PARAMS.CONTENT_MD5.getValue(), UpYunUtils.md5(file, 1024));

        // 设置待上传文件的"访问密钥"
        // 注意：
        // 仅支持图片空！，设置密钥后，无法根据原文件URL直接访问，需带URL后面加上（缩略图间隔标志符+密钥）进行访问
        // 举例：
        // 如果缩略图间隔标志符为"!"，密钥为"bac"，上传文件路径为"/folder/test.jpg"，
        // 那么该图片的对外访问地址为：http://空间域名 /folder/test.jpg!bac
    params.put(PARAMS.CONTENT_SECRET.getValue(), "bac");
    Response result = manager.writeFile("/path/to/file", str, params);
    
```
   
_**注：**
若空间内指定目录已存在相同文件，则会被覆盖，且**不可逆**。若要避免此情况，可以先通过[获取文件信息](#获取文件信息)来判断是否已经存在相同文件_


---

<a name="获取文件信息"></a>
### 获取文件信息

**方法原型：**

```Java
public Response getFileInfo(String filePath)

```

**参数说明：**

* `filePath`  又拍云中文件的路径

**返回值说明：**

* response.headers 信息：

> * `x-upyun-file-type`  文件类型
> * `x-upyun-file-size`  文件大小
> * `x-upyun-file-date`  创建日期
> * `Content-Md5	`  文件的 MD5 值

**举例说明：**

```Java
	String filePath = "/path/to/file";
	System.out.println(filePath + " 的文件信息：" + restManager.getFileInfo(filePath).headers());

```

---

<a name="获取使用量信息"></a>
### 获取使用量信息

**方法原型：**

```Java
	public Resoponse getBucketUsage();
```

**举例说明：**

```Java
	Response response = restManager.getBucketUsage();
	System.out.println("空间总使用量：" + response.body().string() + "B");    	
```

**返回值说明：**

* 返回值单位为 Byte

---

<a name="下载文件"></a>
### 下载文件

**方法原型：**

```Java
	public Response readFile(String filePath)；
```

**参数说明：**

* `filePath`  文件在又拍云存储中的路径
* 
**返回值说明：**

* response.body() 包含文件流信息

**举例说明：**

```Java
    // 直接打印文本内容
    String remoteFilePath = "/path/to/file";
    System.out.println(filePath + " 的文件内容:" + response.body().string());

```

---

<a name="删除文件"></a>
### 删除文件

**方法原型：**

```Java
	public Response deleteFile(String filePath,Map<String, String> params);
```

**参数说明：**

* `filePath`  文件在又拍云的路径
* `params ` 可选参数 可为 null

**返回值说明：**

* response.isSuccessful() 结果为 `true` 删除文件成功

**举例说明：**

```Java
	Response response = restManager.deleteFile(filePath, null);
	System.out.println(filePath + " 删除" + isSuccess(response));
```
<a name="移动文件"></a>
### 移动文件

**方法原型：**

```Java
	public Response moveFile(String path, String sourcePath, Map<String, String> params);
```
**参数说明：**

* `path`	目标路径
*  `sourcePath `	源文件路径

**返回值说明：**

* 返回 Response 

<a name="复制文件"></a>
### 复制文件

**方法原型：**

```Java
	public Response copyFile(String path, String sourcePath, Map<String, String> params);
```
**参数说明：**

* `path`	目标路径
*  `sourcePath `	源文件路径

**返回值说明：**

* 返回 Response 
---

<a name="串行式断点续传"></a>
### 串行式断点续传
初始化 SerialUploader

```java
	SerialUploader resume = new SerialUploader("空间名称", "操作员名称", "操作员密码")
```

设置上传进度监听

```java
	 resume.setOnProgressListener(new BaseUploader.OnProgressListener()
```
设置 MD5 校验

```java
	resume.setCheckMD5(true);
```
开始上传

```java
	public boolean upload(String filePath, String uploadPath,Map<String, String> params)	
```

暂停

```java
	public boolean pause()	
```
继续

```java
	public boolean resume()	
```
---

<a name="并行式断点续传"></a>
### 并行式断点续传
初始化 ParallelUploader

```java
	ParallelUploader paralleUploader = new ParallelUploader("空间名称", "操作员名称", "操作员密码")
```
设置上传进度监听

```java
	 paralleUploader.setOnProgressListener(new ResumeUploader.OnProgressListener()
```
设置 MD5 校验

```java
	paralleUploader.setCheckMD5(true);
```

设置 并行数 校验

```java
	paralleUploader.setParallel(4);
```

开始上传

```java
	public boolean upload(String filePath, String uploadPath,Map<String, String> params)	
```

暂停

```java
	public boolean pause()	
```
继续

```java
	public boolean resume()	
```

**参数说明：**

* `filePath ` 待上传文件路径
*  `uploadPath ` 上传至空间目录
*  `params `	通用可选上传参数见文档 可为null

**详细示例：** 
见 [ResumeUploadDemo](https://github.com/upyun/java-sdk/blob/master/src/main/java/demo/ResumeUploadDemo.java)

---

<a name="表单上传接口"></a>
## 表单上传接口

<a name="初始化FormUploader"></a>
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
## 处理

<a name="异步音视频处理"></a>
### 异步音视频处理

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
### 压缩解压缩

#### 初始化 CompressHandler

```java
	MediaHandler handle = new MediaHandler(BUCKET_NAME, OPERATOR_NAME, OPERATOR_PWD);
```
参数说明：

* `BUCKET_NAME `	 空间名
* `OPERATOR_NAME `  操作员名称
* `OPERATOR_PWD `  操作员密码


#### 发起异步处理请求

```java
	 public Result process(Map<String, Object> params) throws IOException 
```
**参数说明：**

* `params `  参数键值对

详细参数可见 [CompressHandler](https://github.com/upyun/java-sdk/blob/master/src/main/java/com/upyun/CompressHandler.java) 或者[官网 API 文档](http://docs.upyun.com/cloud/unzip/)。

详细示例见 [CompressDemo](https://github.com/upyun/java-sdk/blob/master/src/main/java/demo/CompressDemo.java)

<a name="异步文件拉取"></a>
### 异步文件拉取

#### 初始化 CompressHandler

```java
	MediaHandler handle = new MediaHandler(BUCKET_NAME, OPERATOR_NAME, OPERATOR_PWD);
```
参数说明：同上

#### 发起异步处理请求

```java
	 public Result process(Map<String, Object> params) throws IOException 
```
**参数说明：**

* `params `  参数键值对

详细参数可见 [PullingHandler](https://github.com/upyun/java-sdk/blob/master/src/main/java/com/upyun/PullingHandler.java) 或者[官网 API 文档](http://docs.upyun.com/cloud/spider/)。

详细示例见 [PullingDemo](https://github.com/upyun/java-sdk/blob/master/src/main/java/demo/PullingDemo.java)

<a name="文档转换"></a>
### 文档转换

#### 初始化 CompressHandler

```java
	ConvertHandler handle = new ConvertHandler(BUCKET_NAME, OPERATOR_NAME, OPERATOR_PWD);
```
参数说明：同上

#### 发起异步处理请求

```java
	 public Result process(Map<String, Object> params) throws IOException 
```
**参数说明：**

* `params `  参数键值对

详细参数可见 [ConvertHandler](https://github.com/upyun/java-sdk/blob/master/src/main/java/com/upyun/ConvertHandler.java) 或者[官网 API 文档](http://docs.upyun.com/cloud/uconvert/)。

详细示例见 [JigsawDemo](https://github.com/upyun/java-sdk/blob/master/src/main/java/demo/ConvertDemo.java)

<a name="图片拼接"></a>
### 图片拼接

#### 初始化 JigsawHandler

```java
	JigsawHandler handle = new JigsawHandler(BUCKET_NAME, OPERATOR_NAME, OPERATOR_PWD);
```
参数说明：同上

#### 发起异步处理请求

```java
	 public Result process(Map<String, Object> params) throws IOException 
```
**参数说明：**

* `params `  参数键值对

详细参数可见 [JigsawHandler](https://github.com/upyun/java-sdk/blob/master/src/main/java/com/upyun/JigsawHandler.java) 或者[官网 API 文档](http://docs.upyun.com/cloud/async_image/)。

详细示例见 [JigsawDemo](https://github.com/upyun/java-sdk/blob/master/src/main/java/demo/JigsawDemo.java)


<a name="错误说明"></a>
##错误说明

请参照 [API 错误码表](http://docs.upyun.com/api/errno/#api)

