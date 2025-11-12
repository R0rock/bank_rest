package com.example.bankcards.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

/**
 * Утилитарный класс для работы с номерами банковских карт.
 *
 * <p>Предоставляет методы для шифрования и дешифрования номера карты с помощью AES,
 * а также для маскирования номеров карт (оставляя видимыми только последние 4 цифры).</p>
 */
@Component
public class CardNumberUtil {

    private static final String ALGORITHM = "AES";

    @Value("${encryption.secret}")
    private String secretKey;

    /**
     * Шифрует номер карты с использованием AES и возвращает результат в Base64.
     *
     * @param cardNumber номер карты в открытом виде
     * @return зашифрованный номер карты в формате Base64
     * @throws RuntimeException если возникла ошибка при шифровании
     */
    public String encrypt(String cardNumber) {
        try {
            Key key = generateKey();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedBytes = cipher.doFinal(cardNumber.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting card number", e);
        }
    }

    /**
     * Дешифрует зашифрованный номер карты из Base64 обратно в исходный номер.
     *
     * @param encryptedCardNumber зашифрованный номер карты в формате Base64
     * @return расшифрованный номер карты
     * @throws RuntimeException если возникла ошибка при дешифровании
     */
    public String decrypt(String encryptedCardNumber) {
        try {
            Key key = generateKey();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedCardNumber));
            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting card number", e);
        }
    }

    /**
     * Маскирует номер карты, оставляя видимыми только последние 4 цифры.
     *
     * @param cardNumber номер карты в открытом виде
     * @return замаскированный номер карты, например "**** **** **** 1234"
     */
    public String maskCardNumber(String cardNumber) {
        if (cardNumber == null) {
            return "**** **** **** ****";
        }
        String cleanNumber = cardNumber.replaceAll("\\s+", "");
        if (cleanNumber.length() < 16) {
            return "**** **** **** " + (cleanNumber.length() > 4 ? cleanNumber.substring(cleanNumber.length() - 4) : "****");
        }
        return "**** **** **** " + cleanNumber.substring(cleanNumber.length() - 4);
    }

    /**
     * Маскирует номер карты, принимая зашифрованный номер и расшифровывая его перед маской.
     *
     * @param encryptedCardNumber зашифрованный номер карты
     * @return замаскированный номер карты
     */
    public String maskCardNumberFromEncrypted(String encryptedCardNumber) {
        try {
            String decrypted = decrypt(encryptedCardNumber);
            return maskCardNumber(decrypted);
        } catch (Exception e) {
            return "**** **** **** ****";
        }
    }

    /**
     * Генерирует ключ AES на основе секрета из настроек приложения.
     *
     * @return объект Key для AES
     * @throws Exception если возникла ошибка при генерации ключа
     */
    private Key generateKey() throws Exception {
        byte[] keyValue = secretKey.getBytes("UTF-8");
        byte[] keyBytes = new byte[16]; // AES-128
        System.arraycopy(keyValue, 0, keyBytes, 0, Math.min(keyValue.length, keyBytes.length));
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }
}