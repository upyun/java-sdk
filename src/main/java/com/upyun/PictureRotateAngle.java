package com.upyun;

/**
 * 图片旋转类型
 * User: zjzhai
 * Date: 4/3/14
 */
public enum PictureRotateAngle {

    AUTO("auto"),

    _90("90");


    private final String value;

    private PictureRotateAngle(String val) {
        value = val;
    }

    public String getValue() {
        return value;
    }
}
