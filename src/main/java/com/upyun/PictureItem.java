package com.upyun;

import java.net.HttpURLConnection;
import java.util.Date;

/**
 * User: zjzhai
 * Date: 3/31/14
 */
public class PictureItem {

    private static final String X_UPYUN_WIDTH = "x-upyun-width";
    private static final String X_UPYUN_HEIGHT = "x-upyun-height";
    private static final String X_UPYUN_FRAMES = "x-upyun-frames";
    private static final String X_UPYUN_FILE_TYPE = "x-upyun-file-type";


    private int width;
    private int height;
    private int frames;
    private String type;


    public PictureItem(final int width, final int height, final int frames, final String type) {
        this.width = width;
        this.height = height;
        this.frames = frames;
        this.type = type;
    }

    public PictureItem(final HttpURLConnection responseConn) {
        this(
                Integer.valueOf(responseConn.getHeaderField(X_UPYUN_WIDTH)),
                Integer.valueOf(responseConn.getHeaderField(X_UPYUN_HEIGHT)),
                Integer.valueOf(responseConn.getHeaderField(X_UPYUN_FRAMES)),
                responseConn.getHeaderField(X_UPYUN_FILE_TYPE)
        );
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getFrames() {
        return frames;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "PictureItem{" +
                "width=" + width +
                ", height=" + height +
                ", frames=" + frames +
                ", type='" + type + '\'' +
                '}';
    }
}
