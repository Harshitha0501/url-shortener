package com.emergent.urlshortener.util;

import java.security.SecureRandom;

/**
 * Base62 encoder used to convert numeric IDs / random values into
 * URL-safe short codes.
 */
public final class Base62Encoder {

    private static final char[] ALPHABET =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final int BASE = ALPHABET.length;
    private static final SecureRandom RANDOM = new SecureRandom();

    private Base62Encoder() { }

    public static String encode(long value) {
        if (value == 0) {
            return String.valueOf(ALPHABET[0]);
        }
        StringBuilder sb = new StringBuilder();
        long v = value;
        while (v > 0) {
            sb.append(ALPHABET[(int) (v % BASE)]);
            v /= BASE;
        }
        return sb.reverse().toString();
    }

    /**
     * Generates a random Base62 string of the requested length.
     */
    public static String random(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET[RANDOM.nextInt(BASE)]);
        }
        return sb.toString();
    }
}
