package mro.fantasy.game.plan.impl;

import mro.fantasy.game.Position;
import mro.fantasy.game.Size;
import mro.fantasy.game.engine.GameLibrary;
import mro.fantasy.game.plan.*;
import mro.fantasy.game.resources.GameResource;
import mro.fantasy.game.resources.ResourceLibrary;
import mro.fantasy.game.resources.impl.AbstractGameResource;
import mro.fantasy.game.utils.ValidationUtils;
import mro.fantasy.game.utils.YAMLUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the {@link Plan} interface.
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-02
 */
public class PlanImpl extends AbstractGameResource implements Plan {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(PlanImpl.class);

    /**
     * The size of the plan.
     *
     * @see #getSize()
     */
    private Size size;

    /**
     * A list of all tiles on the plan.
     */
    private List<TileImpl> tiles = new ArrayList<>();

    /**
     * A map with the tile template number as key and a list of tiles in the value section.
     */
    private HashMap<String, List<TileImpl>> tilesByType = new HashMap<>();

    /**
     * Default constructor.
     */
    public PlanImpl() {

    }

    /**
     * Creates a new implementation from the passed YAML data.
     *
     * @param library the game library to resolve resources in the YAML which are only referred by their {@link GameResource#getGameId()}
     * @param data    the data map
     *
     * @throws IllegalArgumentException in case the object cannot be created.
     */
    public PlanImpl(GameLibrary library, Map<String, Object> data) {
        loadFromYAML(library, data);
    }

    @Override
    public Size getSize() {
        return size;
    }

    @Override
    public boolean blocksLineOfSight(Position position) {

        // since on every position multiple fields can be present (dungeon floor on which a monster is placed) we need to check all fields.
        // If a huge monster is placed on a regular dungeon floor for example, the floor would not block the los but the monster would.

        return getFields(position).stream()                                 // get all fields for the given position
                .filter(field -> field.blocksLineOfSight())                 // check if it blocks the line of sight
                .collect(Collectors.toList())
                .size() != 0;                                               // if none block los the position does not block the los.

        // TODO: we may optimize this in the future to avoid iterations, but since I do not expect thousands of tiles on a plan this should be a sufficient solution for now.

    }

    @Override
    public boolean blocksMovement(Position position) {
        return getFields(position).stream()                                 // get all fields for the given position
                .filter(field -> field.blocksMovement())                    // and check if it blocks movement
                .collect(Collectors.toList())
                .size() == 0;                                               // if none blocks movement the position does not block movement.
    }

    @Override
    public boolean canEnter(Position position) {
        return getFields(position).stream()                                 // get all fields for the given position
                .filter(field -> !field.canEnter())                         // and check if a character can enter -> need to invert here to use the size check below
                .collect(Collectors.toList())
                .size() == 0;                                               // if all fields allow entering, the position can be entered.
    }

    @Override
    public List<Field> getFields() {
        return tiles.stream()
                .map(tile -> tile.getFields())                              // iterate over all fields of all tiles on the plan,
                .flatMap(Collection::stream)
                .collect(Collectors.toList());                              // and add them to a list
    }

    @Override
    public List<Field> getFields(Position position) {
        return tiles.stream()
                .map(tile -> tile.getFields())                              // iterate over all fields of all tiles on the plan,
                .flatMap(Collection::stream)
                .filter(f -> f.getPosition().equals(position))              // filter for the position
                .collect(Collectors.toList());                              // and add them to a list
    }

    @Override
    public List<Tile> getTiles(Position position) {
        return tiles.stream()
                .filter(tile -> tile.hasFieldPosition(position))          // all tiles which have a field assigned that matches the passed position
                .collect(Collectors.toList());
    }

    @Override
    public Field getTopField(Position position) {
        var fieldsOnPosition = getFields(position);

        if (fieldsOnPosition == null || fieldsOnPosition.isEmpty()) {            // if no field exist on the position we cannot return anything
            return null;
        }

        Collections.sort(fieldsOnPosition, Comparator.comparingInt(field -> field.getType().getLayer())); // sort by the layer

        return fieldsOnPosition.get(fieldsOnPosition.size() - 1);
    }

    @Override
    public Tile getTopTile(Position position) {
        var tilesOnPosition = getTiles(position);


        if (tilesOnPosition == null || tilesOnPosition.isEmpty()) {            // if no tile exist on the position we cannot return anything
            return null;
        }

        Collections.sort(tilesOnPosition, Comparator.comparingInt(t -> t.getType().getLayer())); // sort by the layer

        return tilesOnPosition.get(tilesOnPosition.size() - 1);  // and return the top layer
    }

    @Override
    public Tile assign(TileTemplate template, Position position, TileRotation rotation) {

        ValidationUtils.requireNonNull(template, "The template cannot be null.");
        ValidationUtils.requireNonNull(position, "The position cannot be null.");
        ValidationUtils.requireNonNull(rotation, "The rotation cannot be null.");

        LOG.debug("Try to assign template ::= [{}] to position ::= [{}] with rotation ::=[{}]", template, position, rotation);

        /*
         *   Example asymmetric tile
         *   ┌─────────┬─────────┬─────────┐
         *   │         │ ▓▓▓▓▓▓▓ │         │
         *   │   1/0   │   1/1   │   1/2   │
         *   │         │ ▓▓▓▓▓▓▓ │         │
         *   ├─────────┼─────────┼─────────┤
         *   │         │         │         │
         *   │   0/0   │   0/1   │   0/2   │
         *   │ 0°      │         │         │
         *   └─────────┴─────────┴─────────┘
         *  and a 4x5 plan.
         *   ┌─────────┬─────────┬─────────┬─────────┐
         *   │         │         │         │         │
         *   │   0/4   │   1/4   │   2/4   │   3/4   │
         *   │         │         │         │         │
         *   ├─────────┼─────────┼─────────┼─────────┤
         *   │         │         │         │         │
         *   │   0/3   │   1/3   │   2/3   │   3/3   │
         *   │         │         │         │         │
         *   ├─────────┼─────────┼─────────┼─────────┤
         *   │         │         │         │         │
         *   │   0/2   │   1/2   │   2/2   │   3/2   │
         *   │         │         │         │         │
         *   ├─────────┼─────────┼─────────┼─────────┤
         *   │         │         │         │         │
         *   │   0/1   │   1/1   │   2/1   │   3/1   │
         *   │         │         │         │         │
         *   ├─────────┼─────────┼─────────┼─────────┤
         *   │         │         │         │         │
         *   │   0/0   │   1/0   │   2/0   │   3/0   │
         *   │         │         │         │         │
         *   └─────────┴─────────┴─────────┴─────────┘
         */

        // the first thing we need to check is the rotation to figure out if the tile can be placed on the plan. Based on the rotation we calculate the number of fields for the
        // dimension of the tile based on the anchor. Since the anchor is always the field (0|0) the normal width and height are always positive.

        int width = switch (rotation) {
            case DEGREE_0 -> template.getSize().columns() - 1;              // we need to correct the height and with by one because we do not count the rom / column of the field
            case DEGREE_90 -> template.getSize().rows() - 1;                // in which the anchor is present.
            case DEGREE_180 -> template.getSize().columns() * -1 + 1;
            case DEGREE_270 -> template.getSize().rows() * -1 + 1;
        };

        int height = switch (rotation) {
            case DEGREE_0 -> template.getSize().rows() - 1;
            case DEGREE_90 -> template.getSize().columns() * -1 + 1;
            case DEGREE_180 -> template.getSize().rows() * -1 + 1;
            case DEGREE_270 -> template.getSize().columns() - 1;
        };

        int x = position.column() + 0;
        int y = position.row() + 0;

        LOG.debug("x ::= [{}], y ::= [{}] width ::= [{}], height ::= [{}]", x, y, width, height);

        // at this point we have a clear overview of the dimension and can check if the tile template will fit to the plan.

        if (x + width > size.columns()) {
            /*   Position (2|1), DEGREE_0
             *   ┌─────────┬─────────┬─────────┬─────────┐
             *   │         │         │         │         │
             *   │   0/4   │   1/4   │   2/4   │   3/4   │
             *   │         │         │         │         │
             *   ├─────────┼─────────┼─────────┼─────────┤
             *   │         │         │         │         │
             *   │   0/3   │   1/3   │   2/3   │   3/3   │
             *   │         │         │         │         │
             *   ├─────────┼─────────┼─────────┼─────────┼─────────┐
             *   │         │         │         │         │ !ERROR! │
             *   │   0/2   │   1/2   │    T1   │    T1   │    T1   │
             *   │         │         │         │         │ !ERROR! │
             *   ├─────────┼─────────┼─────────┼─────────┼─────────┤
             *   │         │         │         │         │ !ERROR! │
             *   │   0/1   │   1/1   │    T1   │    T1   │    T1   │
             *   │         │         │ 0°      │         │ !ERROR! │
             *   ├─────────┼─────────┼─────────┼─────────┼─────────┘
             *   │         │         │         │         │
             *   │   0/0   │   1/0   │   2/0   │   3/0   │
             *   │         │         │         │         │
             *   └─────────┴─────────┴─────────┴─────────┘
             */
            throw new IllegalArgumentException("Tile would excess eastern border of the plan.");
        } else if (x + width < 0) {
            /*   Position (1|1), DEGREE_180
             *             ┌─────────┬─────────┬─────────┬─────────┐
             *             │         │         │         │         │
             *             │   0/4   │   1/4   │   2/4   │   3/4   │
             *             │         │         │         │         │
             *             ├─────────┼─────────┼─────────┼─────────┤
             *             │         │         │         │         │
             *             │   0/3   │   1/3   │   2/3   │   3/3   │
             *             │         │         │         │         │
             *             ├─────────┼─────────┼─────────┼─────────┤
             *             │         │         │         │         │
             *             │   0/2   │   1/2   │   2/2   │   3/2   │
             *             │         │         │         │         │
             *   ┌─────────┼─────────┼─────────┼─────────┼─────────┤
             *   │ !ERROR! │         │    180° │         │         │
             *   │   T1    │    T1   │   T1    │   2/1   │   3/1   │
             *   │ !ERROR! │         │         │         │         │
             *   ├─────────┼─────────┼─────────┼─────────┼─────────┤
             *   │ !ERROR! │         │         │         │         │
             *   │   T1    │    T1   │   T1    │   2/0   │   3/0   │
             *   │ !ERROR! │         │         │         │         │
             *   └─────────┴─────────┴─────────┴─────────┴─────────┘
             */
            throw new IllegalArgumentException("Tile would excess western border of the plan.");
        } else if (y + height < 0) {
            /*   Position (0|1), DEGREE_90
             *   ┌─────────┬─────────┬─────────┬─────────┐
             *   │         │         │         │         │
             *   │   0/4   │   1/4   │   2/4   │   3/4   │
             *   │         │         │         │         │
             *   ├─────────┼─────────┼─────────┼─────────┤
             *   │         │         │         │         │
             *   │   0/3   │   1/3   │   2/3   │   3/3   │
             *   │         │         │         │         │
             *   ├─────────┼─────────┼─────────┼─────────┤
             *   │         │         │         │         │
             *   │   0/2   │   1/2   │   2/2   │   3/2   │
             *   │         │         │         │         │
             *   ├─────────┼─────────┼─────────┼─────────┤
             *   │     90° │         │         │         │
             *   │    T1   │    T1   │   2/1   │   3/1   │
             *   │         │         │         │         │
             *   ├─────────┼─────────┼─────────┼─────────┤
             *   │         │         │         │         │
             *   │    T1   │    T1   │   2/0   │   3/0   │
             *   │         │         │         │         │
             *   ├─────────┼─────────┼─────────┴─────────┘
             *   │ !ERROR! │ !ERROR! │
             *   │    T1   │    T1   │
             *   │ !ERROR! │ !ERROR! │
             *   └─────────┴─────────┘
             */
            throw new IllegalArgumentException("Tile would excess southern border of the plan.");
        } else if (y + height > size.rows()) {
            // I think it should be clear now :D
            throw new IllegalArgumentException("Tile would excess northern border of the plan.");
        }


        TileImpl tile = new TileImpl(template, position, rotation);  // create a new tile and change the positions of that tile to the coordinate system of the plan
        tile.shiftAndRotate();                                       // based on the position and orientation of the tile

        // After the creation we need to check if the tile can be added to the plan or if there was already added a tile before at the same positions the new tile would cover.
        // We need to take into account here, that based on the type the tile can live on the same layer, i.e. a collision will only occur on the same layer. In addition, every
        // tile above layer 1 (floors / walls) can only be placed when a floor tile was already placed before. Finally, character tiles can only be placed if the attribute set
        // of the field allows the placement (does not block movement and can enter).

        // TODO: Implement it...

        // we need to add the new tile to the internal data structures of this class.
        tiles.add(tile);
        addTileByType(tile);


        // Condition.of(tilesByType.containsKey(tile.tileNumber))                  // if the tilesByType list contains already a list for the given tile number
        //         .ifTrue(() -> tilesByType.get(tile.tileNumber).add(tile))       // add the new tile
        //         .orElse(() -> {                                                 // otherwise, create a new list and add it then.
        //             tilesByType.put(tile.tileNumber, new ArrayList<>());
        //             tilesByType.get(tile.tileNumber).add(tile);
        //         });


        return tile;
    }

    /**
     * Adds a tile to the {@link #tilesByType} map. If the type of the tile was not part of the map yet, a new map entry is created.
     *
     * @param tile the tile to add
     */
    private void addTileByType(TileImpl tile) {
        if (!tilesByType.containsKey(tile.getGameId())) {
            tilesByType.put(tile.getGameId(), new ArrayList<>());
        }
        tilesByType.get(tile.getGameId()).add(tile);
    }


    @Override
    public Tile getTileById(String id) {
        return tiles.stream().filter(t -> t.getId().equals(id)).findAny().orElse(null);
    }

    @Override
    public List<Tile> getTiles() {
        return new ArrayList<>(tiles);
    }

    @Override
    public boolean remove(Tile tile) {
        boolean removed = tiles.remove(tile);
        tilesByType.get(tile.getGameId()).remove(tile);
        return removed;
    }

    /**
     * Creates a plan from the passed YAML map. The passed library is used to fetch the templates for the plan by calling the {@link ResourceLibrary#getById(String)} method. If a
     * {@link TileTemplate} is not available an {@link IllegalArgumentException} is thrown.
     * <p>
     * Example YAML file:
     * <pre>{@code
     *   id: BG001
     *   name: Setup Demo
     *   description: Demo Plan to perform a setup
     *   size: 12x6
     *   templates:
     *     - tileNumber: BG002 # Flame Pillar 2x2
     *       tiles:
     *         - position: (1|1)
     *           rotation: 180°
     *         - position: (1|4)
     *           rotation: 270°
     *     - tileNumber: BG001 # Default Floor 2x2
     *       tiles:
     *         - position: (0|2)
     *           rotation: 0°
     *  }</pre>
     *
     * @param library the library with the tiles used in the plan
     * @param data    the data map
     *
     * @throws IllegalArgumentException in case the plan cannot be created.
     */
    @Override
    public void loadFromYAML(GameLibrary library, Map<String, Object> data) {
        super.loadFromYAML(library, data);

        ValidationUtils.requireNonNull(library, "The library cannot be null.");
        ValidationUtils.requireNonNull(data, "The data map cannot be null.");

        this.size = YAMLUtilities.parseSize(data, "size");


        YAMLUtilities.forEach(data, "templates",     // takes the list of YAML objects below the templates section and iterates over it, see forEach method comment
                tMap -> {

                    // load the template which can be reused for all tiles that are handled in the next step, if the template is not available an exception is thrown and
                    // the plan cannot be imported, which is fine
                    TileTemplate template = library.getById(YAMLUtilities.getMandatory(tMap, "tileNumber"));

                    // Iterate over all defined positions to assign the template to the plan. We use the assign method here to validate the plan on the fly, i.e. the
                    // consistency checks are performed automatically.
                    YAMLUtilities.forEach(tMap, "tiles",
                            t -> this.assign(template, YAMLUtilities.parsePosition(t, "position"), YAMLUtilities.parseRotation(t, "rotation")));
                }
        );

    }

    /**
     * Converts the plan back to a YAML map that can be read by the {@link #loadFromYAML(GameLibrary, Map)} method. The tile library is not needed here since the plan was
     * constructed with all relevant information.
     *
     * @return the YAML map
     */
    public Map<String, Object> toYAMLMap() {
        var resultYAML = new HashMap<String, Object>();

        resultYAML.put("id", this.gameId);
        resultYAML.put("name", this.name);
        resultYAML.put("description", this.description);
        resultYAML.put("size", this.size.toString());

        var templateList = new LinkedList<Map<String, Object>>();
        resultYAML.put("templates", templateList);

        /**
         * Example YAML that is generated for the template entry in the map above
         *
         * templates:
         *   - tileNumber: BG002 # Flame Pillar 2x2
         *     tiles:
         *       - position: (0|0)
         *         rotation: 0°
         *       - position: (0|5)
         *         rotation: 90°
         *   - tileNumber: BG001 # Default Floor 2x2
         *     tiles:
         *       - position: (0|2)
         *         rotation: 0°
         *
         *  We have collected the data already in the {@link #tilesByType} map of this class to make fetching it here easier.
         */

        tilesByType.keySet().forEach(key -> {
            var tileTemplate = new HashMap<String, Object>();
            var tilesList = new ArrayList<Map<String, Object>>();
            tileTemplate.put("tileNumber", key);                                 // - tileNumber: BG002 # Flame Pillar 2x2
            tileTemplate.put("tiles", tilesList);                                //   tiles:

            tilesByType.get(key).forEach(tile -> {
                var data = new HashMap<String, Object>();
                data.put("position", tile.getPosition().toString());            //      - position: (0|0)
                data.put("rotation", tile.getRotation().toDegreeString());      //        rotation: 0°
                tilesList.add(data);
            });

            templateList.add(tileTemplate);
        });

        return resultYAML;

    }


    @Override
    public String toString() {
        return "PlanImpl{" +
                       "id='" + gameId + '\'' +
                       ", name='" + name + '\'' +
                       ", description='" + description + '\'' +
                       ", size=" + size +
                       '}';
    }


    @Override
    public Plan copy() {
        PlanImpl plan = new PlanImpl();

        super.copy(plan);

        plan.size = new Size(this.size.columns(), this.size.rows());

        tiles.stream().map(t -> (TileImpl) t.copy()).forEach(t -> {
                    plan.tiles.add(t);
                    plan.addTileByType(t);
                }
        );

        return plan;
    }
}
