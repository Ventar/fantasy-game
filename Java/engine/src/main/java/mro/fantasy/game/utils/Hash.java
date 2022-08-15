package mro.fantasy.game.utils;

import java.security.MessageDigest;

/**
 * Utility class to hash a String.
 *
 * @author Michael Rodenbuecher
 * @since 2022-01-01
 */
public class Hash {

    /**
     * Converts a byte array into a hex String.
     *
     * @param text the text content to hash
     * @return the String representation
     */
    public static String hash(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            //LOG.trace("Calculate Hash for : {}", text.replaceAll("[\\s\\t(\\r?\\n)]+", ""));
            md.update(text.getBytes());
            byte[] hash = md.digest();

            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Converts a byte array into a hex String.
     *
     * @param text   the text content to hash
     * @param regexp the regexp to use to remove data from the String before hashing
     * @return the String representation
     */
    public static String hash(String text, String regexp) {
        return hash(text.replaceAll(regexp, ""));
    }
}
