package mro.fantasy.game.plan.impl;

import mro.fantasy.game.Position;
import mro.fantasy.game.Size;
import mro.fantasy.game.communication.impl.AbstractAudioGameResource;
import mro.fantasy.game.engine.GameLibrary;
import mro.fantasy.game.plan.Field;
import mro.fantasy.game.plan.FieldType;
import mro.fantasy.game.plan.TileTemplate;
import mro.fantasy.game.resources.GameResource;
import mro.fantasy.game.utils.ValidationUtils;
import mro.fantasy.game.utils.YAMLUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of a {@link TileTemplate}.
 *
 * @author Michael Rodenbuecher
 * @since 2022-07-30
 */
public class TileTemplateImpl extends AbstractAudioGameResource implements TileTemplate {

    /**
     * Logger
     */
    public static final Logger LOG = LoggerFactory.getLogger(TileTemplateImpl.class);

    /**
     * The type of the tile.
     *
     * @see #getType()
     */
    protected FieldType type;

    /**
     * The size of the tile.
     *
     * @see #getSize()
     */
    protected Size size;

    /**
     * The fields which build the tile.
     *
     * @see #getFields()
     */
    protected List<FieldImpl> fields = new ArrayList<>();

    /**
     * Default constructor.
     */
    public TileTemplateImpl() {

    }

    /**
     * Creates a new implementation from the passed YAML data.
     *
     * @param library the game library to resolve resources in the YAML which are only referred by their {@link GameResource#getGameId()}
     * @param data    the data map
     *
     * @throws IllegalArgumentException in case the object cannot be created.
     */
    public TileTemplateImpl(GameLibrary library, Map<String, Object> data) {
        loadFromYAML(library, data);
    }

    @Override
    public FieldType getType() {
        return type;
    }

    @Override
    public Size getSize() {
        return size;
    }

    @Override
    public Collection<Field> getFields() {
        return Collections.unmodifiableCollection(fields);
    }

    @Override
    public String toString() {
        return "TileTemplateImpl{" +
                       "type=" + type +
                       ", name='" + name + '\'' +
                       ", size=" + size +
                       ", fields=" + fields +
                       '}';
    }

    /**
     * Creates a new tile template from a YAML entry.
     * <p>
     * <b>Example map entry:</b>
     * <pre>{@code
     *
     * bundleName: Base Game
     * tiles:
     *   - type: DUNGEON_FLOOR
     *     name: Default Floor 2x2
     *     description: basic dungeon floor
     *     number: BG001
     *     audioBundleName: Tile Base Game
     *     audioKey: tiles.base.game.001
     *     sizeColumns: 2
     *     sizeRows: 2
     *     fields:
     *       - column: 0
     *         row: 0
     *         los: false
     *         move: false
     *         enter: true
     *       - column: 1
     *         row: 0
     *         los: false
     *         move: false
     *         enter: true
     *       - column: 0
     *         row: 1
     *         los: false
     *         move: false
     *         enter: true
     *       - column: 1
     *         row: 1
     *         los: false
     *         move: false
     *         enter: true
     * }</pre>
     * which corresponds to a tile:
     * <pre>{@code
     *     ┌─────────┬─────────┐
     *     │         │         │
     *     │   1/0   │   1/1   │
     *     │         │         │
     *     ├─────────┼─────────┤
     *     │         │         │
     *     │   0/0   │   1/0   │
     *     │ 0°      │         │
     *     └─────────┴─────────┘
     * }</pre>
     *
     * <pre>{@code
     * Legend:
     * ─────────────────────────────────────────────
     *   ░  - blocks line of sight
     *   ▓  - block as movement
     *   █  - blocks movement and line of sight
     *   0° - anchor of the tile
     *        (with the rotation 0°, 90° , 180°, 270°
     *   -  - character
     *   x  - plan field without an assigned tile
     *   .  - marker
     * ─────────────────────────────────────────────
     * }</pre>
     *
     * @param data the map with the data
     *
     * @throws IllegalArgumentException if data is missing or invalid
     */
    @Override
    public void loadFromYAML(GameLibrary library, Map<String, Object> data) {

        ValidationUtils.requireNonNull(data, "The data map cannot be null.");

        super.loadFromYAML(library, data);


        // first we load all the data from the map and assign it to the fields in the new tile template. This will not perform any consistency checks but only mandatory checks.

        this.type = FieldType.valueOf(YAMLUtilities.getMandatory(data, "type"));


        this.size = new Size(YAMLUtilities.getMandatory(data, "sizeColumns"), YAMLUtilities.getMandatory(data, "sizeRows"));

        YAMLUtilities.getMandatoryList(data, "fields").forEach(f ->       // we take every map from the YAML and convert it to
                                                                       this.fields.add(FieldImpl.fromYAMLMap(f, this.type))              // a new field instance that is added the new tile.
        );

        // afterwards we need to check the consistency, i.e. if we have the correct number of fields in the expected positons.

        List<Field> tmpList = new ArrayList<>(this.fields);                                     // we create a copy of the field list and remove every field from the list
        for (int col = 0; col < this.size.columns(); col++) {                                   // by iterating over the columns and rows which are defined by the size
            for (int row = 0; row < this.size.rows(); row++) {                                  // of the tile template.
                final int x = col;                                                              // If the list is empty afterwards we are sure that every possible position
                final int y = row;                                                              // in the template has a field assigned and the exact number of fields
                tmpList.removeIf(f -> f.getPosition().equals(new Position(x, y)));              // were provided. Otherwise an exception is thrown.
            }
        }

        if (!tmpList.isEmpty())
            throw new IllegalArgumentException("The passed field configuration does not match the size of the tile template");

        if (this.fields.stream().filter(f -> f.isAnchor()).collect(Collectors.toList()).size() != 1)
            throw new IllegalArgumentException("exactly one anchor field has to be defined for a tile template.");
    }

}
