package util;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class EncryptedProperties extends Properties {

    private final char[] encryptionKey;

    public EncryptedProperties(String encryptionKey) {
        super();
        this.encryptionKey = encryptionKey.toCharArray();
        this.put("util.EncryptedProperties.Password.Test", "1");
    }

    @Override
    public synchronized void load(InputStream inStream) throws IOException {
        Properties properties = new Properties();
        properties.load(inStream);
        properties.stringPropertyNames().forEach(propertyName -> {
            super.put(propertyName, properties.getProperty(propertyName));
        });

        try {
            if (!stringPropertyNames().contains("util.EncryptedProperties.Password.Test")) {
                throw new IOException("wrong password.");
            }
        } catch (Exception e) {
            throw new IOException("wrong password.");
        }
    }

    @Override
    public synchronized Object put(Object key, Object value) {
        String property = getProperty(key.toString());
        if (property != null) {
            remove(key);
        }

        if (key instanceof String && value instanceof String) {
            String encryptedKey = CryptoHelper.encrypt((String) key, encryptionKey);
            String encryptedValue = CryptoHelper.encrypt((String) value, encryptionKey);
            super.put(encryptedKey, encryptedValue);
            return property;
        }

        throw new NotImplementedException();
    }

    @Override
    public synchronized Object putIfAbsent(Object key, Object value) {
        throw new NotImplementedException();
    }


    @Override
    public synchronized void putAll(Map<?, ?> t) {
        throw new NotImplementedException();
    }

    @Override
    public String getProperty(String key) {
        Set<String> encryptedPropertyNames = super.stringPropertyNames();
        for (String encryptedPropertyName : encryptedPropertyNames) {
            String decryptedPropertyName = CryptoHelper.decrypt(encryptedPropertyName, encryptionKey);
            if (decryptedPropertyName.equals(key)) {
                return CryptoHelper.decrypt(super.getProperty(encryptedPropertyName), encryptionKey);
            }
        }
        return null;
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        String property = getProperty(key);
        return property != null && !property.isEmpty() ? property : defaultValue;
    }

    @Override
    public synchronized Object setProperty(String key, String value) {
        String encryptedKey = CryptoHelper.encrypt(key, encryptionKey);
        String encryptedValue = CryptoHelper.encrypt(value, encryptionKey);
        return super.setProperty(encryptedKey, encryptedValue);
    }

    @Override
    public Enumeration<?> propertyNames() {
        throw new NotImplementedException();
    }

    @Override
    public Set<String> stringPropertyNames() {
        return super.stringPropertyNames()
                .stream()
                .map(propertyName -> CryptoHelper.decrypt(propertyName, encryptionKey))
                .collect(Collectors.toSet());
    }

    @Override
    public synchronized Object remove(Object key) {
        if (key instanceof String) {
            for (String encryptedKey : super.stringPropertyNames()) {
                String decryptedKey = CryptoHelper.decrypt(encryptedKey, encryptionKey);
                if (decryptedKey.equals(key)) {
                    return super.remove(encryptedKey);
                }
            }
        }

        return null;
    }
}
