package com.ycf.liveborrowsample.infrastructure.dingtalk;

import com.ycf.liveborrowsample.config.DingTalkProperties;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

@Component
public class DingTalkCallbackCrypto {

    private static final int RANDOM_PREFIX_LENGTH = 16;
    private static final int PKCS7_BLOCK_SIZE = 32;

    private final DingTalkProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    public DingTalkCallbackCrypto(DingTalkProperties properties) {
        this.properties = properties;
    }

    public String decrypt(String signature, String timestamp, String nonce, String encrypted) {
        requireCallbackConfigured();
        verifySignature(signature, timestamp, nonce, encrypted);
        try {
            byte[] aesKey = decodeAesKey();
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");
            IvParameterSpec iv = new IvParameterSpec(copyOf(aesKey, 16));
            cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);

            byte[] original = cipher.doFinal(Base64.getDecoder().decode(encrypted));
            byte[] bytes = removePadding(original);

            ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN);
            buffer.position(RANDOM_PREFIX_LENGTH);
            int jsonLength = buffer.getInt();
            byte[] messageBytes = new byte[jsonLength];
            buffer.get(messageBytes);
            byte[] ownerKeyBytes = new byte[buffer.remaining()];
            buffer.get(ownerKeyBytes);

            String ownerKey = new String(ownerKeyBytes, StandardCharsets.UTF_8);
            String expectedOwnerKey = properties.resolveCallbackOwnerKey();
            if (expectedOwnerKey != null && !expectedOwnerKey.isBlank() && !expectedOwnerKey.equals(ownerKey)) {
                throw new DingTalkApiException("回调 ownerKey 校验失败");
            }
            return new String(messageBytes, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException ex) {
            throw new DingTalkApiException("回调消息解密失败");
        }
    }

    public EncryptedCallbackResponse encryptResponse(String plainText, String timestamp, String nonce) {
        requireCallbackConfigured();
        String encrypt = encrypt(plainText);
        String msgSignature = sign(timestamp, nonce, encrypt);
        return new EncryptedCallbackResponse(msgSignature, timestamp, nonce, encrypt);
    }

    public String sign(String timestamp, String nonce, String encrypted) {
        try {
            List<String> parts = new ArrayList<>();
            parts.add(properties.getCallbackToken());
            parts.add(timestamp);
            parts.add(nonce);
            parts.add(encrypted);
            parts.sort(Comparator.naturalOrder());
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            return HexFormat.of().formatHex(digest.digest(String.join("", parts).getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("SHA-1 not supported", ex);
        }
    }

    private void verifySignature(String signature, String timestamp, String nonce, String encrypted) {
        String expected = sign(timestamp, nonce, encrypted);
        if (signature == null || !signature.equals(expected)) {
            throw new DingTalkApiException("回调签名校验失败");
        }
    }

    private String encrypt(String plainText) {
        try {
            byte[] aesKey = decodeAesKey();
            byte[] random = new byte[RANDOM_PREFIX_LENGTH];
            secureRandom.nextBytes(random);

            byte[] messageBytes = plainText.getBytes(StandardCharsets.UTF_8);
            byte[] ownerKeyBytes = properties.resolveCallbackOwnerKey().getBytes(StandardCharsets.UTF_8);

            ByteBuffer buffer = ByteBuffer.allocate(
                RANDOM_PREFIX_LENGTH + 4 + messageBytes.length + ownerKeyBytes.length
            ).order(ByteOrder.BIG_ENDIAN);
            buffer.put(random);
            buffer.putInt(messageBytes.length);
            buffer.put(messageBytes);
            buffer.put(ownerKeyBytes);

            byte[] padded = addPadding(buffer.array());

            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");
            IvParameterSpec iv = new IvParameterSpec(copyOf(aesKey, 16));
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);
            return Base64.getEncoder().encodeToString(cipher.doFinal(padded));
        } catch (GeneralSecurityException ex) {
            throw new DingTalkApiException("回调消息加密失败");
        }
    }

    private byte[] decodeAesKey() {
        return Base64.getDecoder().decode(properties.getCallbackAesKey() + "=");
    }

    private void requireCallbackConfigured() {
        if (properties.getCallbackToken() == null || properties.getCallbackToken().isBlank()
            || properties.getCallbackAesKey() == null || properties.getCallbackAesKey().isBlank()
            || properties.resolveCallbackOwnerKey() == null || properties.resolveCallbackOwnerKey().isBlank()) {
            throw new DingTalkApiException("请先配置钉钉回调 Token、EncodingAESKey 和 ownerKey");
        }
    }

    private byte[] addPadding(byte[] plainText) {
        int amountToPad = PKCS7_BLOCK_SIZE - (plainText.length % PKCS7_BLOCK_SIZE);
        if (amountToPad == 0) {
            amountToPad = PKCS7_BLOCK_SIZE;
        }

        byte[] padded = new byte[plainText.length + amountToPad];
        System.arraycopy(plainText, 0, padded, 0, plainText.length);
        for (int i = plainText.length; i < padded.length; i++) {
            padded[i] = (byte) amountToPad;
        }
        return padded;
    }

    private byte[] removePadding(byte[] decrypted) {
        int pad = decrypted[decrypted.length - 1] & 0xFF;
        if (pad < 1 || pad > PKCS7_BLOCK_SIZE) {
            return decrypted;
        }
        byte[] result = new byte[decrypted.length - pad];
        System.arraycopy(decrypted, 0, result, 0, result.length);
        return result;
    }

    private byte[] copyOf(byte[] source, int length) {
        byte[] result = new byte[length];
        System.arraycopy(source, 0, result, 0, length);
        return result;
    }

    public record EncryptedCallbackResponse(String msgSignature, String timeStamp, String nonce, String encrypt) {
    }
}
