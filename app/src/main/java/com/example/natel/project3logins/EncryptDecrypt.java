package com.example.natel.project3logins;

import android.util.Log;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

// Encrypt and decrypt strings.
public class EncryptDecrypt {
    // note: key should be 16 ASCII characters which will create 128 bit key

    public String encrypt (String unencryptedText, String key) {
        String encryptedText = "";
        try {
            Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            byte[] encrypted = cipher.doFinal(unencryptedText.getBytes());

            // Convert from byte array to hex StringBuilder
            StringBuilder sb = new StringBuilder();
            for (byte b : encrypted) {
                sb.append(String.format("%02X", b));
            }

            encryptedText = sb.toString();
            Log.d("Message", "Encrypted text: " + encryptedText);
        } catch (Exception e) {
            Log.d("Message", "In exception" + e);
        }
        return encryptedText;
    } // end method encrypt

    public String decrypt (String encryptedText, String key) {
        String decryptedText = "";

        // call helper function included in this class
        byte[] encrypted = hexStringToByteArray(encryptedText);

        try {
            Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            decryptedText = new String(cipher.doFinal(encrypted));
            Log.d("Message", "Decrypted message: " + decryptedText);
        } catch (Exception e) {
            Log.d("Message", "In exception" + e);
        }
        return decryptedText;
    } // end method decrypt

    // Helper function to convert hex string to a byte array.
    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    } // end helper function

} // end class EncryptDecrypt
