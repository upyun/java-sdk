package com.upyun;

import org.json.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class UpYunUtils {

    public static final String VERSION = "upyun-java-sdk/4.2.2";
    private static final String SEPARATOR = "/";

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

    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    public static String sign(String method, String date, String path, String userName, String password, String md5) throws UpException {

        StringBuilder sb = new StringBuilder();
        String sp = "&";
        sb.append(method);
        sb.append(sp);
        sb.append(path);

        sb.append(sp);
        sb.append(date);

        if (md5 != null && md5.length() > 0) {
            sb.append(sp);
            sb.append(md5);
        }
        String raw = sb.toString().trim();
        byte[] hmac = null;
        try {
            hmac = calculateRFC2104HMACRaw(password, raw);
        } catch (Exception e) {
            throw new UpException("calculate SHA1 wrong.");
        }

        if (hmac != null) {
            return "UPYUN " + userName + ":" + Base64Coder.encodeLines(hmac).trim();
        }

        return null;
    }

    public static byte[] calculateRFC2104HMACRaw(String key, String data)
            throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
        byte[] keyBytes = key.getBytes();
        SecretKeySpec signingKey = new SecretKeySpec(keyBytes, HMAC_SHA1_ALGORITHM);

        // Get an hmac_sha1 Mac instance and initialize with the signing key
        Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        mac.init(signingKey);
        // Compute the hmac on input data bytes
        return mac.doFinal(data.getBytes());
    }

    private static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static String formatPath(String bucketName, String path) {

        if (!isEmpty(path)) {

            // 去除前后的空格
            path = path.trim();

            // 确保路径以"/"开头
            if (!path.startsWith(SEPARATOR)) {
                return SEPARATOR + bucketName + SEPARATOR + path;
            }
        }

        return SEPARATOR + bucketName + path;
    }

    /**
     * 获取 GMT 格式时间戳
     *
     * @return GMT 格式时间戳
     */
    public static String getGMTDate() {
        SimpleDateFormat formater = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        formater.setTimeZone(TimeZone.getTimeZone("GMT"));
        return formater.format(new Date());
    }
}
