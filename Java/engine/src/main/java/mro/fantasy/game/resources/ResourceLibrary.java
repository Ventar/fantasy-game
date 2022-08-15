package mro.fantasy.game.resources;

/**
 * A collection of {@link GameResource}s from multiple {@link ResourceBundle}s. Actually a library just collects all data in a single facade implementation to allow easy access to
 * all game related data.
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-05
 */
public interface ResourceLibrary<T extends GameResource> extends ResourceBundle<T> {


}
