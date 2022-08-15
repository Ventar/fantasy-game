package mro.fantasy.game.engine.gamedata;

import mro.fantasy.game.utils.ValidationUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A set that contains values for multiple {@link GameDataType}s. The set it initializes with a value of zero for every type and can be used to increase or decrease these values
 * with utility methods.
 */
public class GameDataSet {

    /**
     * The map with data values.
     */
    private final Map<GameDataType, Integer> data;

    /**
     * Creates a new game data  set.
     */
    public GameDataSet() {
        data = Arrays.stream(GameDataType.values()).collect(Collectors.toMap(a -> a, a -> 0)); // initialize all known data types with a value of zero.
    }

    /**
     * Returns the value of the given data type.
     *
     * @param type the type to retrieve
     *
     * @return the value
     */
    public Integer getValue(GameDataType type) {
        return data.get(type); // due to the initialization in the constructor there will always be an entry.
    }

    /**
     * Adds a value to the given data type. The value can be negative, in that case a subtraction is performed.
     *
     * @param type  the data type
     * @param value the value to add
     *
     * @return this data set.
     */
    public GameDataSet add(GameDataType type, Integer value) {
        int current = data.get(type);
        data.put(type, current + value);
        return this;
    }

    /**
     * Clears (set to zero) the passed data type in this set
     *
     * @param type the type to clear
     *
     * @return this data set
     */
    public GameDataSet clear(GameDataType type) {
        data.put(type, 0);
        return this;
    }

    /**
     * Creates a copy of this data set.
     *
     * @return the copy
     */
    public GameDataSet copy() {
        return combine(new GameDataSet()); // this + 0 = this :)
    }

    /**
     * Performs a combination of this data set with the passed one. The passed set and this one are not changed, a new one is created instead. Combination means that the integer
     * values for all data types are added, if one of the data types in a set is negative it is a subtraction.
     *
     * @param set the set to combine with this set.
     *
     * @return a new, combined, set
     *
     * @throws NullPointerException if the passed set is <code>null</code>
     */
    public GameDataSet combine(GameDataSet set) {

        ValidationUtils.requireNonNull(set, "The passed game data set cannot be null");

        GameDataSet combined = new GameDataSet();

        Arrays.stream(GameDataType.values()).forEach(key -> {
            int valueA = this.data.get(key);                      // get the attribute value from this set
            int valueB = set.data.get(key);                       // get the attribute value from the set to combine
            combined.data.put(key, valueA + valueB);              // set the combined value; since the default is 0 and was not change we can override it here
        });

        return combined;
    }

    /**
     * Performs a combination of this game data set with the passed ones. The passed sets and this one are not changed, a new one is created instead. Combination means that the
     * integer values for all data sets are added, if one of the data types in a set is negative it is a subtraction.
     *
     * @param sets the set to combine with this set.
     *
     * @return a new, combined, set
     */
    public GameDataSet combine(Collection<GameDataSet> sets) {

        ValidationUtils.requireNonNull(sets, "The passed gama data sets cannot be null");

        GameDataSet combined = new GameDataSet();

        for (GameDataSet set : sets) {
            combined = combined.combine(set);
        }

        return combined;
    }

}
