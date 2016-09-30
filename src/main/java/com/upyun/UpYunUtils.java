package main.java.com.upyun;

import org.json.JSONObject;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Map;

public class UpYunUtils {

    public static final String VERSION = "upyun-java-sdk/3.11";

    /**
     * 计算policy
     *
     * @param paramMap
     * @return
     */
    public static String getPolicy(Map<String, Object> paramMap) {

        JSONObject obj = new JSONObject(paramMap);
        return Base64Coder.encodeString(obj.toString());
    }

    /**
     * 计算签名
     *
     * @param policy
     * @param secretKey
     * @return
     */
    public static String getSignature(String policy,
                                      String secretKey) {
        return md5(policy + "&" + secretKey);
    }

    /**
     * 计算md5Ø
     *
     * @param string
     * @return
     */
    public static String md5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 is unsupported", e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MessageDigest不支持MD5Util", e);
        }
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

    public static String md5(File file, int blockSize) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            FileInputStream in = new FileInputStream(file);
            byte[] buffer = new byte[blockSize];
            int length;
            while ((length = in.read(buffer)) > 0) {
                messageDigest.update(buffer, 0, length);
            }
            byte[] hash = messageDigest.digest();
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                if ((b & 0xFF) < 0x10) hex.append("0");
                hex.append(Integer.toHexString(b & 0xFF));
            }
            return hex.toString();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("file not found", e);
        } catch (IOException e) {
            throw new RuntimeException("file get md5 failed", e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MessageDigest不支持MD5Util", e);
        }
    }

    public static String md5(byte[] bytes) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MessageDigest不支持MD5Util", e);
        }
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }
}
