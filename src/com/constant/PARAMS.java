package com.constant;

/**
 * 其他额外参数的键值和参数值
 */
public enum PARAMS {

	/**
	 * 缩略图类型
	 * <p>
	 * 使用场景：上传图片时若无需保存原图，而只需某种特定大小的缩略图，比如说用户头像。
	 * <p>
	 * 说明：该参数必须搭配 KEY_X_GMKERL_VALUE 使用，否则无效。另外，使用该参数后将不保存原图，切忌。
	 * <p>
	 * 可选参数：<br>
	 * 1)VALUE_FIX_MAX("fix_max")："限定最长边，短边自适应"<br>
	 * 2)VALUE_FIX_MIN("fix_min")："限定最短边，长边自适应"<br>
	 * 3)VALUE_FIX_WIDTH_OR_HEIGHT("fix_width_or_height")："限定宽度和高度"<br>
	 * 4)VALUE_FIX_WIDTH("fix_width")："限定宽度，高度自适应"<br>
	 * 5)VALUE_FIX_HEIGHT("fix_height")："限定高度，宽度自适应"<br>
	 * 6)VALUE_FIX_BOTH("fix_both")："固定宽度和高度"<br>
	 * 7)VALUE_FIX_SCALE("fix_scale")："等比例缩放"<br>
	 * 8)VALUE_SQUARE("square")："方块图，固定高固定宽"<br>
	 * 
	 * @see 参数举例：http://wiki.upyun.com/index.php?title=缩略图方式差别举例
	 */
	KEY_X_GMKERL_TYPE("x-gmkerl-type"),

	/**
	 * 缩略图参数值
	 * <p>
	 * 说明：该参数必须搭配 KEY_X_GMKERL_TYPE 使用，否则无效。具体的值需要根据 KEY_X_GMKERL_TYPE 而定。
	 */
	KEY_X_GMKERL_VALUE("x-gmkerl-value"),

	/**
	 * 缩略图质量：图片压缩质量，默认 95
	 * <p>
	 * 使用场景：用户上传高保真图片，但自身业务又无需太高质量的图片时，可以设置该参数减少文件保存的大小，从而减少空间的使用量。
	 * <p>
	 * 说明：使用该参数后将不保存原图，切忌。
	 */
	KEY_X_GMKERL_QUALITY("x-gmkerl-quality"),

	/**
	 * 图片锐化：默认锐化（true）
	 * <p>
	 * 使用场景：图片处理后质量太差，可以使用该参数模糊边缘，提高图片的清晰度或者焦距程度，使图片特定区域的色彩更加鲜明。
	 * <p>
	 * 说明：锐化不是万能的，很容易使图片不真实；另外，也无法通过锐化达到原图的效果。
	 */
	KEY_X_GMKERL_UNSHARP("x-gmkerl-unsharp"),

	/**
	 * 缩略图版本
	 * <p>
	 * 使用场景：快速处理原图，生成自定义的缩略图。
	 * <p>
	 * 说明：使用该参数前需要创建好缩略图版本号；另外，使用该参数后将不保存原图，切忌。
	 * 
	 * @see http://wiki.upyun.com/index.php?title=如何创建自定义缩略图
	 */
	KEY_X_GMKERL_THUMBNAIL("x-gmkerl-thumbnail"),

	/**
	 * 图片旋转
	 * <p>
	 * 使用场景：待上传的图片若是倾斜的，使用该参数可以直接进行强制的或自动的扶正。
	 * <p>
	 * 说明：只接受"auto"，"90"，"180"，"270"四种参数，其中"auto"参数根据图片 EXIF
	 * 中的信息进行自动扶正，若图片没有 EXIF 信息，则该参数无效。另外，使用该参数后将不保存原图，切忌。
	 * 
	 * @see http://wiki.upyun.com/index.php?title=图片旋转
	 */
	KEY_X_GMKERL_ROTATE("x-gmkerl-rotate"),

	/**
	 * 图片裁剪
	 * <p>
	 * 使用场景：只需要保存待上传图片的某一个部分，比如用户上传头像图片进行裁剪。
	 * <p>
	 * 说明：参数格式为x,y,width,height，且需要满足 x >= 0 && y >=0 && width > 0 && height
	 * > 0
	 * 
	 * @see http://wiki.upyun.com/index.php?title=图片裁剪
	 */
	KEY_X_GMKERL_CROP("x-gmkerl-crop"),

	/**
	 * 是否保留exif信息
	 * <p>
	 * 使用场景：对于原图包含EXIF信息，在上传图片时又进行了“破坏性处理”（比如裁剪、缩略、自定义版本等），
	 * upyun默认会删除原图的EXIF信息。 此时搭配该参数可以保留原图的EXIF信息。比如旅游应用从缩略图中获取具体的地理信息。
	 * <p>
	 * 说明：仅搭配"破坏性处理"的参数使用时有效，其他处理均无效；另外key对应的值仅设置为"true"时有效；
	 */
	KEY_X_GMKERL_EXIF_SWITCH("x-gmkerl-exif-switch"),

	/**
	 * 创建目录
	 * <p>
	 * 说明：SDK内部使用
	 */
	KEY_MAKE_DIR("folder"),

	/**
	 * 缩略图类型之 "限定最长边，短边自适应"，参数为像素值，如: 150
	 */
	VALUE_FIX_MAX("fix_max"),
	/**
	 * 缩略图类型之 "限定最短边，长边自适应"，参数为像素值，如: 150
	 */
	VALUE_FIX_MIN("fix_min"),
	/**
	 * 缩略图类型之 "限定宽度和高度"，参数为像素值，如: 150x130
	 */
	VALUE_FIX_WIDTH_OR_HEIGHT("fix_width_or_height"),
	/**
	 * 缩略图类型之 "限定宽度，高度自适应"，参数为像素值，如: 150
	 */
	VALUE_FIX_WIDTH("fix_width"),
	/**
	 * 缩略图类型之 "限定高度，宽度自适应"，参数为像素值，如: 150
	 */
	VALUE_FIX_HEIGHT("fix_height"),
	/**
	 * 缩略图类型之 "方块图，固定高固定宽"，参数为像素值，如: 150
	 */
	VALUE_SQUARE("square"),
	/**
	 * 缩略图类型之 "固定宽度和高度"，参数为像素值，如: 150x130
	 */
	VALUE_FIX_BOTH("fix_both"),
	/**
	 * 缩略图类型之 "等比例缩放"，参数为比例值（1-99），如: 50
	 */
	VALUE_FIX_SCALE("fix_scale"),

	/**
	 * 图片旋转之 "自动扶正"
	 */
	VALUE_ROTATE_AUTO("auto"),
	/**
	 * 图片旋转之 "旋转90度"
	 */
	VALUE_ROTATE_90("90"),
	/**
	 * 图片旋转之 "旋转180度"
	 */
	VALUE_ROTATE_180("180"),
	/**
	 * 图片旋转之 "旋转270度"
	 */
	VALUE_ROTATE_270("270");

	private final String value;

	private PARAMS(String val) {
		value = val;
	}

	public String getValue() {
		return value;
	}
}
