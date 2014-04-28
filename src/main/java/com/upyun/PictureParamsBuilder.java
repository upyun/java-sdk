package com.upyun;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 图片处理参数构建器
 * User: zjzhai
 * Date: 4/28/14
 */
public class PictureParamsBuilder {

    private Map<String, String> params = new HashMap<String, String>();

    public static PictureParamsBuilder create() {
        return new PictureParamsBuilder();
    }

    public Map<String, String> build() {
        return Collections.unmodifiableMap(params);
    }

    /**
     * 缩图的设置
     *
     * @param type  缩略图的类型
     * @param value 缩略类型对应的参数值，单位为像素,特别注意fix_width_or_height/fix_both：width x height(如 200x150)
     * @return
     */
    public PictureParamsBuilder picThumbnail(ThumbnailType type, Integer... value) {
        assert value != null && value.length > 0 && value.length <= 2;
        String realValue = "" + value[0];
        if (value.length > 1) {
            realValue += "x" + value[1];
        }
        params.put("x-gmkerl-type", type.getValue());
        params.put("x-gmkerl-value", realValue);
        return this;
    }

    /**
     * 设置缩略图的名称
     *
     * @param nameValue
     * @return
     */
    public PictureParamsBuilder picThumbnailName(String nameValue) {
        assert !_.isEmpty(nameValue);
        params.put("x-gmkerl-thumbnail", nameValue);
        return this;
    }

    /**
     * 图片压缩质量，范围1~100，默认 95。
     *
     * @param qualityValue
     * @return
     */
    public PictureParamsBuilder picThumbnailQuality(int qualityValue) {
        qualityValue = (qualityValue < 1) ? 1 : qualityValue;
        qualityValue = (qualityValue > 100) ? 100 : qualityValue;

        params.put("x-gmkerl-quality", "" + qualityValue);
        return this;
    }

    /**
     * 关闭锐化
     *
     * @return
     */
    public PictureParamsBuilder picThumbnailUnsharpen() {
        params.put("x-gmkerl-quality", "false");
        return this;
    }

    /**
     * 打开锐化
     *
     * @return
     */
    public PictureParamsBuilder picThumbnailSharpen() {
        params.put("x-gmkerl-quality", "true");
        return this;
    }

    /**
     * 自定义的缩略图版本名称，比如 small
     *
     * @return
     */
    public PictureParamsBuilder picThumbnailVersion(String versionValue) {
        assert !_.isEmpty(versionValue);
        params.put("x-gmkerl-thumbnail", versionValue);
        return this;
    }

    /**
     * 不保留原图的 EXIF 信息
     * 若原图带有 EXIF 信息并做缩略处理时，
     * 默认将删除 EXIF 信息
     *
     * @return
     */
    public PictureParamsBuilder unSaveExif() {
        params.put("x-gmkerl-exif-switch", "false");
        return this;
    }

    /**
     * 保留原图的 EXIF 信息
     * 若原图带有 EXIF 信息并做缩略处理时，
     * 默认将删除 EXIF 信息
     *
     * @return
     */
    public PictureParamsBuilder saveExif() {
        params.put("x-gmkerl-exif-switch", "true");
        return this;

    }

    /**
     * 图片旋转角度
     *
     * @param angle
     * @return
     */
    public PictureParamsBuilder picRotateAngle(PictureRotateAngle angle) {
        params.put("x-gmkerl-rotate", angle.getValue());
        return this;
    }

    /**
     * 图片裁剪 的相关参数的设置
     * <p/>
     * (x,y)：左上角坐标；
     * width：要裁剪的宽度；height：要裁剪的高度
     * x >= 0 && y >=0 && width > 0 && height > 0 且必须是正整型
     *
     * @param x
     * @param y
     * @param width
     * @param height
     * @return
     */
    public PictureParamsBuilder picCutCutting(int x, int y, int width, int height) {
        assert x >= 0 && y >= 0 && width > 0 && height > 0;
        params.put("x-gmkerl-crop", x + "," + y + "," + width + "," + height + "");
        return this;
    }


    /**
     * 图片旋转
     *
     * @param pictureRotateAngle
     * @return
     */
    public static PictureParamsBuilder createPicRotateAngle(PictureRotateAngle pictureRotateAngle) {
        return create().picRotateAngle(pictureRotateAngle);
    }

    /**
     * 图片裁剪
     *
     * @param x
     * @param y
     * @param width
     * @param height
     * @return
     */
    public static PictureParamsBuilder createPicCutCutting(int x, int y, int width, int height) {
        return create().picCutCutting(x, y, width, height);
    }
}
