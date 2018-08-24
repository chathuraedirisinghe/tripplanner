package com.jlanka.tripplanner.Database;

import android.util.Log;

import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Decrypt {

    public static String decrypt(String encrypted) {
        String key = "emobilityjlpande";
        SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), "AES");
        byte[] decode = base64ToByteArray(encrypted);

        Log.w("Encrypted Text : ",encrypted);
        Log.w("ET to Byte Array : " , Arrays.toString(decode));

        try {
            Cipher cipher2 = Cipher.getInstance("AES/ECB/NoPadding","BC");
            cipher2.init(Cipher.DECRYPT_MODE, skeySpec);
            String de = new String(cipher2.doFinal(decode), "UTF-8");
            Log.w("Decrypted Text : ",de);
            return de;
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return null;
    }

    public static byte[] base64ToByteArray(String text){
        byte[] decodedString = new byte[0];
        try {
            decodedString = Base64.decodeBase64(text.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return decodedString;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}

