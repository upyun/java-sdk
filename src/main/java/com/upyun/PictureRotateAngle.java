package com.upyun;

/**
 * 图片旋转角度
 * User: zjzhai
 * Date: 4/3/14
 */
public enum PictureRotateAngle {

    AUTO("auto"),
    _90("90"),
    _180("180"),
    _270("270");


    private final String value;

    private PictureRotateAngle(String val) {
        value = val;
    }

    public String getValue() {
        return value;
    }
}
