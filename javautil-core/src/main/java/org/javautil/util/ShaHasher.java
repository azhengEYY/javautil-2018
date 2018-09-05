package org.javautil.util;



import java.security.MessageDigest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

public class ShaHasher {

    public static String hashAsBase64(final String text) {
        final byte[] result = hashAsByteArray(text);
        return new String(Base64.encodeBase64(result));
    }

    public static String hashAsHex(final String text) {
        final byte[] result = hashAsByteArray(text);
        return new String(Hex.encodeHex(result));
    }

    public static byte[] hashAsByteArray(final String text) {
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            return messageDigest.digest(text.getBytes("UTF-8"));
        } catch (final Exception e) {
            throw new RuntimeException("failure while hashing ", e);
        }
    }
}
