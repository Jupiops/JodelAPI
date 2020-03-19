package com.fr31b3u73r.jodel;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Formatter;
import java.util.Locale;
import java.util.Random;

public class JodelHelper {
    /**
     * Gets a random color for a new Jodel
     *
     * @return Color string
     */
    public static String getRandomColor() {
        String[] allColors = {JodelPostColor.GREEN, JodelPostColor.BLUE, JodelPostColor.RED,
                JodelPostColor.ORANGE, JodelPostColor.TEAL, JodelPostColor.YELLOW};

        int idx = new Random().nextInt(allColors.length);
        return (allColors[idx]);
    }

    private static String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        String hexString = formatter.toString();
        formatter.close();
        return hexString;
    }

    protected static String calculateHMAC(String key, String data)
            throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(signingKey);
        return toHexString(mac.doFinal(data.getBytes())).toUpperCase();
    }

    protected static String randomizeGeography(String randomize) {
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
        otherSymbols.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("##.0000000", otherSymbols);

        Random random = new Random();
        double lat = random.nextInt(100_000);
        lat = (lat / 1_000_000);
        lat = random.nextBoolean() ? (lat * -1) : lat;
        return df.format((Double.parseDouble(randomize) + lat));
    }
}
