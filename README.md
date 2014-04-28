又拍云 Java SDK
---

又拍云存储Java SDK，基于 [又拍云存储HTTP REST API接口](http://wiki.upyun.com/index.php?title=HTTP_REST_API接口) 开发，适用于Java 6及以上版本。



## 目录
* [使用方法](#使用方法)
* [云存储基础接口](#云存储基础接口)
  * [准备操作](#准备操作)
  * [上传文件](#上传文件)
  * [下载文件](#下载文件)
  * [获取文件信息](#获取文件信息)
  * [删除文件](#删除文件)
  * [创建目录](#创建目录)
  * [删除目录](#删除目录)
  * [获取目录文件列表](#获取目录文件列表)
  * [获取使用量情况](#获取使用量情况)
* [图片处理接口](#图片处理接口)
  * [缩略图](#缩略图)
  * [图片裁剪](#图片裁剪)
  * [图片旋转](#图片旋转)
* [错误代码表](#错误代码表)

<a name="使用方法"></a>
## 使用方法
* maven

	1. 加入OSC仓库
				<repositories>
            		<repository>
            			<id>nexus</id>
            			<name>local private nexus</name>
            			<url>http://maven.oschina.net/content/groups/public/</url>
            			<releases>
            				<enabled>true</enabled>
            			</releases>
            			<snapshots>
            				<enabled>false</enabled>
            			</snapshots>
            		</repository>
            	</repositories>

	1. 加入依赖

    		<dependency>
    		  <groupId>com.upyun</groupId>
    		  <artifactId>java-sdk</artifactId>
    		  <version>2.0.0</version>
    		</dependency>


* 其他方法

[下载](http://maven.oschina.net/service/local/repositories/thirdparty/content/com/upyun/java-sdk/2.0.0/java-sdk-2.0.0.jar)jar包，加入到类路径。



<a name="云存储基础接口"></a>
## 云存储基础接口

<a name="准备操作"></a>
### 准备操作

##### 创建空间
大家可通过[又拍云主站](https://www.upyun.com/login.php)创建自己的个性化空间。具体教程请参见[“创建空间”](http://wiki.upyun.com/index.php?title=创建空间)。

##### 初始化UpYun    
    UpYunClient client = UpYunClient.create("空间名称", "授权操作员名称", "操作员密码");

若不了解`授权操作员`，请参见[“授权操作员”](http://wiki.upyun.com/index.php?title=创建操作员并授权)

##### 是否开启debug模式：默认不开启
   client.enableDebug();

##### 手动设置超时时间：默认为30秒
    client.timeout(60);

##### 选择最优的接入点
    
根据国内的网络情况，又拍云存储API目前提供了电信、联通网通、移动铁通三个接入点。

若没有明确进行设置，`UpYun`默认将根据网络条件自动选择接入点。

接入点可使用相信的API来切换：

* 根据网络条件自动选择接入点： upyun.selectAutoAPIEntry();
* 电信接入点：client.selectTelecomAPIEntry();
* 联通网通接入点：client.selectUnicomAPIEntry();
* 移动铁通接入点：client.selectMobileAPIEntry();

_**注：**建议大家根据服务器网络状况，手动设置合理的接入点已获取最佳的访问速度。_



<a name="上传文件"></a>
### 上传文件

    UpYunClient client = UpYunClient.create("空间名称", "授权操作员名称", "操作员密码");
    
    // 例1：直接将纯文本内容上传到又拍云
    client.uploadFile(fileRemotePath, "test content");

    // 例2：将文件上传到又拍云
    File file = new File(localFilePath);
    client.uploadFile(fileRemotePath, file);



##### 其他说明
* 文件上传成功后，可直接通过`http://空间名.b0.upaiyun.com/savePath`来访问文件

##### 递归创建目录

不论上传文件还是图片，上传路径都有可能是多级的，如`/a/b/c/a.txt`。而在上传前调用`recursionMkDir`方法则会自动帮助你
递归创建这些目录。 只支持自动创建10级以内的父级目录。

    client.recursionMkDir().uploadFile(fileRemotePath, file);


##### 注意事项
* 如果空间内`savePath`已经存在文件，将进行覆盖操作，并且是**不可逆**的。所以如果需要避免文件被覆盖的情况，可以先通过[获取文件信息](#获取文件信息)操作来判断是否已经存在旧文件。
* 图片类空间只允许上传图片类文件，其他文件上传时将返回“不是图片”的错误。
* 如果上传失败，则会抛出异常。

##### 可选操作
1. 上传文件时可进行文件的`MD5`校验：若又拍云服务端收到的文件MD5值与用户设置的不一致，将返回 `406 Not Acceptable` 错误。对于需要确保上传文件的完整性要求的业务，可以设置该参数。
```
    client.contentMD5(UpYun.md5(file));
```

2. 图片类空间若设置过[缩略图版本号](http://wiki.upyun.com/index.php?title=如何创建自定义缩略图)，即可使用原图保护功能（**文件类空间无效**）：
```
    upyun.setFileSecret("abc");  
```

    **特别说明**：  
    * 原图保护功能需要设置一个自定义的密钥（只有您自己知道，如上面的`abc`）。待文件保存成功后，将无法根据`http://空间名.b0.upaiyun.com/savePath`直接访问上传的文件，而是需要在 URL 后面加上`缩略图间隔标志符+密钥`进行访问。比如当[缩略图间隔标志符](http://wiki.upyun.com/index.php?title=如何使用自定义缩略图)为`!`，密钥为`abc`，上传的文件路径为`/dir/sample.jpg`，那么该图片的对外访问地址为: `http://空间名.b0.upaiyun.com/dir/sample.jpg!abc`
    * **原图保护密钥若与[缩略图版本号](http://wiki.upyun.com/index.php?title=如何创建自定义缩略图)名称相同，则在对外访问时将被视为是缩略图功能，而原图将无法访问，请慎重使用。**




<a name="下载文件"></a>
### 下载文件

    UpYunClient client = UpYunClient.create("空间名称", "授权操作员名称", "操作员密码");
    
    // 例1：直接读取文本内容
    String data = client.readFileText(fileRemotePath);

    // 例2：下载文件
    File file = new File(localFilePath); // 下载后文件的存储路径
    boolean result = client.downloadFile(fileRemotePath, file);



##### 注意事项
* 如果文件不存在，抛`UpYunNotFoundException`异常



<a name="获取文件信息"></a>
### 获取文件信息

    UpYunClient client = UpYunClient.create("空间名称", "授权操作员名称", "操作员密码");

    FileItem item = client.getFileInfo(fileRemotePath);




<a name="删除文件"></a>
### 删除文件

    UpYunClient client = UpYunClient.create("空间名称", "授权操作员名称", "操作员密码");
    
    client.deleteFile(fileRemotePath);
    
##### 注意事项
* 如果文件不存在，抛`UpYunNotFoundException`异常



<a name="创建目录"></a>
### 创建目录

    UpYunClient client = UpYunClient.create("空间名称", "授权操作员名称", "操作员密码");
    
    // 方法1：创建一级目录
    String dir1 = ;
    client.unRecursionMkDir().createFolder("/a/");

    // 方法2：创建多级目录，自动创建父级目录（最多10级）
    client.recursionMkDir().createFolder("/a/b/c/d/");

##### 其他说明
* 待创建的目录路径必须以斜杠 `/` 结尾
* 若空间相同目录下已经存在同名的文件，则将返回`不允许创建目录`的错误



<a name="删除目录"></a>
### 删除目录

    client.deleteFolder("/a/b/c/d");

##### 其他说明
* 该操作只能删除单级目录，不能一次性同时删除多级目录，比如当存在`/dir1/dir2/dir3/`目录时，不能试图只传递`/dir1/`来删除所有目录。
* 若待删除的目录`dir`下还存在任何文件或子目录，将返回`不允许删除`的错误。比如当存在`/dir1/dir2/dir3/`目录时，将无法删除`/dir1/dir2/`目录。



<a name="获取目录文件列表"></a>
### 获取目录文件列表

    List<FileItem> items = client.listFiles(fileRemotePath);

##### 其他说明
* 可以循环获取`items`中文件的“名称”、“类型”（文件或目录）、“创建时间”和“文件大小”
* 若`dir`目录不存在任何内容时，将直接返回一个空的`ArrayList`
* 若`dir`目录不存在时，则将返回`不存在目录`的错误




<a name="获取使用量情况"></a>
### 获取使用量情况

    long usage = client.getBucketUsage();


##### 其他说明
* 使用量的单位为 `byte`，比如`1M`的使用量将以`1048576`这样的数字返回

<a name="图片处理接口"></a>
## 图片处理接口
对于图片的自定义处理，又拍云存储支持以下两种方式：

1. [自定义版本](http://wiki.upyun.com/index.php?title=如何使用自定义缩略图)方式
2. 上传图片时传递图片处理参数

虽然两种方式都能够达到图片处理的效果，但存在以下区别：

| 区别点 |  自定义版本方式  |  参数处理方式  | 
| ------------ | ---------- | ---------- |
| 是否保留原图 | 是，各个缩略图都在这个原图的基础上制作 | 否，只保留处理后的图片，若再使用缩略图版本号的方式来访问（这种方法是可行的），则将在处理后的图片基础上制作 |
| 空间使用量 | 以原图的大小计算使用量，后续各个版本的缩略图都不会计算在用户的空间使用量中 | 以处理后的图片大小计算使用量，大小视具体的处理参数而定 |
| 灵活性 | 可通过修改自定义版本的参数来满足变化的需求，参数修改后若没有自动刷新缓存，则可以[手动强制刷新](https://www.upyun.com/purge.php)来确保新参数生效 | 只能调整代码中的处理参数，且原先保存的图片无法自动更新 |

我们更推荐大家使用自定义版本的方式对图片进行处理，但您可以根据自己业务的使用场景来选用合适的方式。

以下内容只是介绍“传递图片处理参数”的方法。

<a name="缩略图"></a>
### 缩略图

            // 要传到upyun后的文件路径
            String filePath = DIR_ROOT + "gmkerl.jpg";

            // 本地待上传的图片文件
            File file = new File(SAMPLE_PIC_FILE);


            PictureParamsBuilder builder = PictureParamsBuilder.create();
            builder.picThumbnail(ThumbnailType.VALUE_FIX_BOTH, 150, 150)
                    //图片质量
                    .picThumbnailQuality(95)
                    //锐化
                    .picThumbnailSharpen()
                    .picThumbnailName("small");


            // 上传文件，并自动创建父级目录（最多10级）
            PictureItem pictureItem = upYunClient.recursionMkDir()
                    .uploadPicture(filePath, file, builder);

##### 说明
`PictureParamsBuilder`为设置图片的参数的构建器。
`ThumbnailType`为缩略图的类型。

##### 其他说明
* 图片处理参数的具体使用方法，请参考[标准API上传文件](http://wiki.upyun.com/index.php?title=标准API上传文件)
* 缩略图功能只能处理图片文件；若上传非图片文件且传递了图片处理参数时，将返回`不是图片`的错误



<a name="图片裁剪"></a>
### 图片裁剪

            // 要传到upyun后的文件路径
            String filePath = DIR_ROOT + "crop.jpg";

            // 本地待上传的图片文件
            File file = new File(SAMPLE_PIC_FILE);

            // 图片裁剪功能具体可参考：http://wiki.upyun.com/index.php?title=图片裁剪
            // 设置图片裁剪，参数格式：x,y,width,height
            PictureParamsBuilder builder = PictureParamsBuilder.createPicCutCutting(50, 50, 300, 300);

            // 上传图片
            PictureItem pictureItem = upYunClient.uploadPicture(filePath, file, builder);


##### 其他说明
* 参数格式暂时只支持：`x,y,width,height`。比如`0,0,100,100`表示从左上角顶点裁剪`100px × 100px`大小的图片
* 具体可参考[图片裁剪](http://wiki.upyun.com/index.php?title=图片裁剪)



<a name="图片旋转"></a>
### 图片旋转

        // 要传到upyun后的文件路径
        String filePath = DIR_ROOT + "rotate.jpg";

        // 本地待上传的图片文件
        File file = new File(SAMPLE_PIC_FILE);

        // 图片旋转功能具体可参考：http://wiki.upyun.com/index.php?title=图片旋转
        PictureParamsBuilder builder = PictureParamsBuilder.createPicRotateAngle(PictureRotateAngle._90);

        // 上传图片
        PictureItem pictureItem = upYunClient.uploadPicture(filePath, file, builder);

##### 其他说明
* 暂时只接受"auto"，"90"，"180"，"270"四种参数(由PictureRotateAngle枚举提供)，其中`auto`处理时需要图片包含`EXIF`信息
* 具体可参考[图片旋转](http://wiki.upyun.com/index.php?title=图片旋转)



<a name="错误代码表"></a>
## 错误代码表

| HTTP状态码 |  返回代码  |  描述   |
| ------------ | ---------- | ---------- |
| 400 | Bad Request | 错误请求(如 URL 缺少空间名) |
| 401 | Unauthorized | 访问未授权 |
| 401 | Sign error | 签名错误(操作员和密码,或签名格式错误) |
| 401 | Need Date Header | 发起的请求缺少 Date 头信息 |
| 401 | Date offset error | 发起请求的服务器时间错误，请检查服务器时间是否与世界时间一致|
| 403 | Not Access | 权限错误(如非图片文件上传到图片空间)|
| 403 | File size too max | 单个文件超出大小(100Mb 以内) |
| 403 | Not a Picture File | 图片类空间错误码，非图片文件或图片文件格式错误。针对图片空间只允许上传 jpg/png/gif/bmp/tif 格式。|
| 403 | Picture Size too max | 图片类空间错误码，图片尺寸太大。针对图片空间，图片总像素在 200000000 以内。|
| 403 | Bucket full | 空间已用满 |
| 403 | Bucket blocked | 空间被禁用,请联系管理员|
| 403 | User blocked | 操作员被禁用|
| 403 | Image Rotate Invalid Parameters | 图片旋转参数错误|
| 403 | Image Crop Invalid Parameters | 图片裁剪参数错误|
| 404 | Not Found | 获取文件或目录不存在；上传文件或目录时上级目录不存在|
| 406 | Not Acceptable(path) | 目录错误（创建目录时已存在同名文件；或上传文件时存在同名目录)|
| 503 | System Error | 系统错误 |