package com.yixia.cipher;

import android.text.TextUtils;
import android.util.Base64;

import com.yixia.util.Logger;

import java.security.MessageDigest;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Author: zhangwy(张维亚)
 * 创建时间：2017/4/5 下午2:39
 * 修改时间：2017/4/5 下午2:39
 * Description: AES加密解密算法封装
 * 每次需要new出一个实例(提供有newInstance接口)
 * encrypt(String text)加密字符串
 * decrypt(byte[] array)解密byte数组
 * decrypt(String cipher)解密字符串
 */
public class AESCipher {

    private final String TAG = "AESCipher";

    public static AESCipher newInstance() {
        try {
            return new AESCipher();
        } catch (Exception e) {
            return null;
        }
    }

    public static AESCipher newInstance(String key) {
        try {
            return new AESCipher(key);
        } catch (Exception e) {
            return null;
        }
    }

    private final String KEY_DEFAULT = "d41d8cd98f00b204e9800998ecf8427e";
    private final Cipher cipher;
    private final SecretKeySpec key;
    private AlgorithmParameterSpec spec;

    private AESCipher() throws Exception {
        this(null);
    }

    private AESCipher(String keyStr) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update((TextUtils.isEmpty(keyStr) ? KEY_DEFAULT : keyStr).getBytes("UTF-8"));
        byte[] keyBytes = new byte[32];
        System.arraycopy(digest.digest(), 0, keyBytes, 0, keyBytes.length);
        cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        key = new SecretKeySpec(keyBytes, "AES");
        spec = getIV();
    }

    private AlgorithmParameterSpec getIV() {
        byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,};
        return new IvParameterSpec(iv);
    }

    public String encrypt(String text) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);
            byte[] encrypted = cipher.doFinal(text.getBytes("UTF-8"));
            return new String(Base64.encode(encrypted, Base64.DEFAULT), "UTF-8");
        } catch (Exception e) {
            Logger.e(TAG, "encrypt", e);
        }
        return "";
    }

    public String decrypt(byte[] array) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            byte[] bytes = Base64.decode(array, Base64.DEFAULT);
            byte[] decrypted = cipher.doFinal(bytes);
            return new String(decrypted, "UTF-8");
        } catch (Exception e) {
            Logger.e(TAG, "decrypt", e);
        }
        return "";
    }

    public String decrypt(String cipherText) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            byte[] bytes = Base64.decode(cipherText, Base64.DEFAULT);
            byte[] decrypted = cipher.doFinal(bytes);
            return new String(decrypted, "UTF-8");
        } catch (Exception e) {
            Logger.e(TAG, "decrypt", e);
        }
        return "";
    }
}
