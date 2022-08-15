package mro.fantasy.game.resources.impl;

import mro.fantasy.game.engine.GameLibrary;
import mro.fantasy.game.resources.GameResource;
import mro.fantasy.game.utils.YAMLUtilities;

import java.util.Map;
import java.util.Objects;

/**
 * Default implementation of a game resource.
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-06
 */
public abstract class AbstractGameResource implements GameResource {

    /**
     * The name of the tile.
     *
     * @see #getName()
     */
    protected String name;

    /**
     * The description of the tile.
     *
     * @see #getDescription()
     */
    protected String description;

    /**
     * The unique ID of the plan.
     *
     * @see #getGameId()
     */
    protected String gameId;

    /**
     * Loads the needed fields from the YAML map. This part of the parsing process loads the {@link #getGameId()}, {@link #getDescription()} and {@link #getName()} part of the game
     * resource in the map.
     *
     * @param library the game library to resolve entities which are only referred by their {@link GameResource#getGameId()}.
     * @param data    the YAML data map.
     */
    public void loadFromYAML(GameLibrary library, Map<String, Object> data) {
        this.gameId = YAMLUtilities.getOptional(data, "id");
        this.name = YAMLUtilities.getOptional(data, "name");
        this.description = YAMLUtilities.getOptional(data, "description");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getGameId() {
        return gameId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractGameResource that = (AbstractGameResource) o;
        return gameId.equals(that.gameId);
    }

    /**
     * Copies all data from this abstract resource to the passed resource.
     *
     * @param newResource the resource to fill with the data of this instance
     */
    protected void copy(AbstractGameResource newResource) {
        newResource.gameId = gameId;
        newResource.description = description;
        newResource.name = name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameId);
    }
}
