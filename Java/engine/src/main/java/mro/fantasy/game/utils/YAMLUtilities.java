package mro.fantasy.game.utils;

import mro.fantasy.game.Position;
import mro.fantasy.game.Size;
import mro.fantasy.game.plan.TileRotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class YAMLUtilities {

    /**
     * Logger
     */
    public static final Logger LOG = LoggerFactory.getLogger(YAMLUtilities.class);


    /**
     * Utility class.
     */
    private YAMLUtilities() {

    }

    /**
     * Tries to load a YAML map from the passed resource name in the classpath
     *
     * @param name name of the desired resource
     *
     * @return the YAML map.
     *
     * @throws NullPointerException If {@code name} is {@code null}
     */
    public static Map<String, Object> fromClasspath(String name) {
        Yaml yaml = new Yaml(); // not thread safe !
        return yaml.load(YAMLUtilities.class.getResourceAsStream(name));
    }

    /**
     * Tries to return the appropriate value for the given key from the underlying data map. If the value is not present {@code null} is returned. If the value in the map cannot be
     * cast to the correct data type, too.
     *
     * @param map the map that should contain the key.
     * @param key the key to fetch from the data map
     * @param <T> the data type to which the value of the passed key should be converted
     *
     * @return
     */
    public static <T> T getOptional(Map<String, Object> map, String key) {
        if (map.containsKey(key)) {
            try {
                return (T) map.get(key);
            } catch (ClassCastException e) {
                LOG.trace("Cannot cast key ::=[{}]: ", key, e);
            }
        }

        return null;
    }

    /**
     * Tries to return the appropriate value for the given key from the underlying data map. If the value is not present or if the value in the map cannot be cast to the correct
     * data type an exception is thrown.
     *
     * @param map the map that should contain the key.
     * @param key the key to fetch from the data map
     * @param <T> the data type to which the value of the passed key should be converted
     *
     * @return the content of the given key
     *
     * @throws IllegalArgumentException if the key cannot be resolved to a value.
     */
    public static <T> T getMandatory(Map<String, Object> map, String key) {
        if (!map.containsKey(key)) {
            throw new IllegalArgumentException("The key ::= [" + key + "] is not present in the map");
        }

        try {
            return (T) map.get(key);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("The key ::= [" + key + "] cannot be cast to the target type", e);
        }
    }

    /**
     * Returns a list of YAML objects which are represented by maps. The passed key has to provide a list with at least one entry.
     *
     * @param map the map that should contain the key.
     * @param key the key to fetch from the data map
     *
     * @return the list with YAML objects
     *
     * @throws IllegalArgumentException if the key cannot be resolved to a list with at least one entry.
     */
    public static List<Map<String, Object>> getMandatoryList(Map<String, Object> map, String key) {
        List<Map<String, Object>> data = getMandatory(map, key);

        if (data.isEmpty()) {
            throw new IllegalArgumentException("The list ::= [" + key + "] does not have at least one entry");
        }

        return data;
    }

    /**
     * Performs an {@link Stream#forEach(Consumer)} operation on the results of the {@link #getMandatoryList(Map, String)} method of this utility class. This method is a shortcut
     * for a common case to handle the game YAML files and makes the source code more readable in the parser implementation.
     *
     * @param map      the map that should contain the key.
     * @param key      the key to fetch from the data map
     * @param consumer the operation to execute on the entries
     */
    public static void forEach(Map<String, Object> map, String key, Consumer<Map<String, Object>> consumer) {
        getMandatoryList(map, key).stream().forEach(consumer::accept);
    }

    /**
     * Tries to create a new position from the passed YAML entry that hat to follow the syntax (x|y)
     *
     * @param map the map that should contain the key.
     * @param key the key to fetch from the data map
     *
     * @return the position
     *
     * @throws IllegalArgumentException if the key cannot be resolved to a value or the parsing of the position fails.
     */
    public static Position parsePosition(Map<String, Object> map, String key) {
        String data = getMandatory(map, key);
        ValidationUtils.requireTrue(data.matches("\\(\\d{1,2}\\|\\d{1,2}\\)"), "The key ::= [" + key + "] does not have the format (x|y)");

        String[] coordinates = data.split("\\|");

        return new Position(Integer.valueOf(coordinates[0].substring(1)), Integer.valueOf(coordinates[1].substring(0, coordinates[1].length() - 1)));
    }


    /**
     * Tries to create a new size from the passed YAML entry that hat to follow the syntax (x|y)
     *
     * @param map the map that should contain the key.
     * @param key the key to fetch from the data map
     *
     * @return the size
     *
     * @throws IllegalArgumentException if the key cannot be resolved to a value or the parsing of the size fails.
     */
    public static Size parseSize(Map<String, Object> map, String key) {
        String data = getMandatory(map, key);
        ValidationUtils.requireTrue(data.matches("\\d{1,2}x\\d{1,2}"), "The key ::= [" + key + "] does not have the format {columns}x{rows}");

        String[] coordinates = data.split("x");

        return new Size(Integer.valueOf(coordinates[0]), Integer.valueOf(coordinates[1]));
    }

    /**
     * Tries to create a new size from the passed YAML entry that hat to follow the syntax 0°, 90°, 180°, 270°
     *
     * @param map the map that should contain the key.
     * @param key the key to fetch from the data map
     *
     * @return the size
     *
     * @throws IllegalArgumentException if the key cannot be resolved to a value or the parsing of the size fails.
     */
    public static TileRotation parseRotation(Map<String, Object> map, String key) {
        String data = getMandatory(map, key);
        ValidationUtils.requireTrue(
                data.equals("0°") ||
                        data.equals("90°") ||
                        data.equals("180°") ||
                        data.equals("270°"),
                "The key ::= [" + key + "] does not have the " +
                        "format x°");

        return switch (data) {
            case "0°" -> TileRotation.DEGREE_0;
            case "90°" -> TileRotation.DEGREE_90;
            case "180°" -> TileRotation.DEGREE_180;
            case "270°" -> TileRotation.DEGREE_270;
            default -> throw new IllegalArgumentException("Invalid rotation value.");
        };

    }

}
