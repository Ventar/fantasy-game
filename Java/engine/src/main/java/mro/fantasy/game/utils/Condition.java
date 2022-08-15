package mro.fantasy.game.utils;

/**
 * Abbreviation for short if-else statements to reduce code size and increase readability in a style of {@link java.util.Optional}
 */
public class Condition {

    /**
     * The value to check.
     */
    private final boolean check;

    /**
     * Creates a new condition.
     *
     * @param check the value to check
     */
    private Condition(boolean check) {
        this.check = check;
    }

    /**
     * Creates a new condition object.
     *
     * @param condition the condition
     *
     * @return the condition object
     */
    public static Condition of(boolean condition) {
        return new Condition(condition);
    }

    /**
     * Executes the passed code if the {@link #check} field of the condition is {@code true}
     *
     * @param code the code to execute
     */
    public Condition ifTrue(Runnable code) {
        if (check) {
            code.run();
        }

        return this;
    }

    /**
     * Executes the passed code if the {@link #check} field of the condition is {@code false}
     *
     * @param code the code to execute
     */
    public Condition orElse(Runnable code) {
        if (!check) {
            code.run();
        }
        return this;
    }
}
