package com.upyun;

/**
 * 缩略图的方式
 * User: zjzhai
 * Date: 4/2/14
 */
public enum ThumbnailType {

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
    VALUE_FIX_SCALE("fix_scale");
    private final String value;

    private ThumbnailType(String val) {
        value = val;
    }

    public String getValue() {
        return value;
    }
}
