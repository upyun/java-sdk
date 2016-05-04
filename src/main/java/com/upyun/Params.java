package main.java.com.upyun;

public class Params {
    /*bucket	是	保存所上传的文件的 UPYUN 空间名
    save-key	是	保存路径，如: '/path/to/file.ext'，可用占位符 [注 1]
    expiration	是	请求的过期时间，UNIX 时间戳（秒）
    allow-file-type	否	文件类型限制，制定允许上传的文件扩展名
    content-length-range	否	文件大小限制，格式：min,max，单位：字节，如 102400,1024000，允许上传 100Kb～1Mb 的文件
    content-md5	否	所上传的文件的 MD5 校验值，UPYUN 根据此来校验文件上传是否正确
    content-secret	否	原图访问密钥 [注 2]
    content-type	否	UPYUN 默认根据扩展名判断，手动指定可提高精确性
    image-width-range	否	图片宽度限制，格式：min,max，单位：像素，如 0,1024，允许上传宽度为 0～1024px 之间
    image-height-range	否	图片高度限制，格式：min,max，单位：像素，如 0,1024，允许上传高度在 0～1024px 之间
    notify-url	否	异步通知 URL，见 [通知规则]
            return-url	否	同步通知 URL，见 [通知规则]
    x-gmkerl-thumbnail	否	缩略图版本名称，仅支持图片类空间，可搭配其他 x-gmkerl-* 参数使用 [注 3]
    x-gmkerl-type	否	缩略类型 [注 4]
    x-gmkerl-value	否	缩略类型对应的参数值 [注 4]
    x-gmkerl-quality	否	默认 95缩略图压缩质量
    x-gmkerl-unsharp	否	默认锐化（true）是否进行锐化处理
    x-gmkerl-rotate	否	图片旋转（顺时针），可选：auto，90，180，270 之一
    x-gmkerl-crop	否	图片裁剪，格式：x,y,width,height，均需为正整型
    x-gmkerl-exif-switch	否	是否保留 exif 信息，仅在搭配 x-gmkerl-crop，x-gmkerl-type，x-gmkerl-thumbnail 时有效。
    ext-param	否	额外参数，UTF-8 编码，并小于 255 个字符 [注 5]*/
    //必选参数
    public final static String BUCKET = "bucket";
    public final static String SAVE_KEY = "save-key";
    public final static String EXPIRATION = "expiration";
    //可选参数
    public final static String ALLOW_FILE_TYPE = "allow-file-type";
    public final static String CONTENT_LENGTH_RANGE = "content-length-range";
    public final static String CONTENT_MD5 = "content-md5";
    public final static String CONTENT_SECRET = "content-secret";
    public final static String CONTENT_ECRET = "content-secret";
    public final static String CONTENT_TYPE = "content-type";
    public final static String IMAGE_WIDTH_RANGE = "image-width-range";
    public final static String NOTIFY_URL = "notify-url";
    public final static String RETURN_URL = "return-url";
    public final static String X_GMKERL_THUMBNAIL = "x-gmkerl-thumbnail";
    public final static String X_GMKERL_TYPE = "x-gmkerl-type";
    public final static String X_GMKERL_VALUE = "x-gmkerl-value";
    public final static String X_GMKERL_QUALITY = "x-gmkerl-quality";
    public final static String X_GMKERL_UNSHARP = "x-gmkerl-unsharp";
    public final static String X_GMKERL_ROTATE = "x-gmkerl-rotate";
    public final static String X_GMKERL_CROP = "x-gmkerl-crop";
    public final static String X_GMKERL_EXIF_SWITCH = "x-gmkerl-exif-switch";
    public final static String EXT_PARAM = "ext-param";


    public static final String SIGNATURE = "signature";
    public static final String POLICY = "policy";
    // 空间保存路径
    public static final String PATH = "path";
    public static final String SAVE_TOKEN = "save_token";
    public static final String TOKEN_SECRET = "token_secret";
    public static final String FILE_SIZE = "file_size";
    public static final String FILE_MD5 = "file_hash";
    // 分块上传情况
    public static final String STATUS = "status";
    public static final String BLOCK_NUM = "file_blocks";
    public static final String BLOCK_INDEX = "block_index";
    public static final String BLOCK_MD5 = "block_hash";
}
