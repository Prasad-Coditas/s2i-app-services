package com.snap2buy.themobilebackend.util;

import java.security.MessageDigest;

/**
 * Created by Anoop on 9/7/16.
 */
public class CryptoUtil {

    public static String encrypt(String plainText){
    	String encryptedText = plainText;
		try {
			MessageDigest mDigest = MessageDigest.getInstance("SHA1");
			byte[] result = mDigest.digest(plainText.getBytes());
	        StringBuffer sb = new StringBuffer();
	        for (int i = 0; i < result.length; i++) {
	            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
	        }
	        encryptedText = sb.toString();
		} catch (Exception e) {
			//DO NOTHING , GROUND THIS EXCEPTION AND RETURN ORIGINAL STRING
		}
         
        return encryptedText;
    }
}
