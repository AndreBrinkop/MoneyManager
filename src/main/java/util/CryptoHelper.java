package util;

import org.jasypt.util.text.BasicTextEncryptor;

public class CryptoHelper {

    public static String encrypt(String content, char[] key) {
        BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
        textEncryptor.setPasswordCharArray(key);
        return textEncryptor.encrypt(content);
    }

    public static String decrypt(String encrypted, char[] key) {
        BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
        textEncryptor.setPasswordCharArray(key);
        return textEncryptor.decrypt(encrypted);
    }

}
