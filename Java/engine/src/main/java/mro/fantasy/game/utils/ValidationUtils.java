package mro.fantasy.game.utils;

/**
 * Utility class similar to the Java {@link java.util.Objects} class with additional functionality.
 *
 * @author Michael Rodenbuecher
 * @since 2022-07-21
 */
public final class ValidationUtils {

    private ValidationUtils() {
        throw new AssertionError("How did you end up here ??? Don't play with illegal code manipulation !");
    }

    public static <T> T requireNonNull(T obj, String message) {
        if (obj == null) {throw new NullPointerException(message);}
        return obj;
    }

    public static void requireFalse(Boolean term, String message) {
        if (term) {throw new IllegalArgumentException(message);}

    }

    public static void requireTrue(Boolean term, String message) {
        if (!term) {throw new IllegalArgumentException(message);}

    }

}
