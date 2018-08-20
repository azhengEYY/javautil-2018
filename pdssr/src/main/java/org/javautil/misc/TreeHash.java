package org.javautil.misc;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;

public class TreeHash {

    @SuppressWarnings("rawtypes")
    public static String hash(Map map) {
        TreeMap tree = null;
        if (!(map instanceof TreeMap)) {
            tree = new TreeMap();
            for (final Object k : map.keySet()) {
                tree.put(k, map.get(k));
            }
        } else {
            tree = (TreeMap) map;
        }
        final StringBuilder b = new StringBuilder();
        for (final Object k : map.keySet()) {
            b.append("\"");
            b.append(k.toString());
            b.append("\"");
            b.append(":");
            final Object o = tree.get(k);
            b.append(o == null ? "null" : k.toString());
            b.append(",");
        }
        final String text = b.toString().substring(0, b.length() - 1);
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
        final byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
        final StringBuffer hexString = new StringBuffer();
        for (final byte element : hash) {
            final String hex = Integer.toHexString(0xff & element);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }

}
