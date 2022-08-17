package mro.fantasy.game.plan.impl;

import mro.fantasy.game.Position;
import mro.fantasy.game.Size;
import mro.fantasy.game.plan.Field;
import mro.fantasy.game.plan.Tile;
import mro.fantasy.game.plan.TileRotation;
import mro.fantasy.game.plan.TileTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of a {@link Tile}.
 *
 * @author Michael Rodenbuecher
 * @since 2022-07-31
 */
public class TileImpl extends TileTemplateImpl implements Tile {

    /**
     * The id of this tile.
     *
     * @see #getGameId()
     */
    private String id;

    /**
     * The position of this template on the plan.
     *
     * @see #getPosition()
     */
    private Position position;

    /**
     * The orientation of this tile.
     *
     * @see #getRotation()
     */
    private TileRotation orientation;


    /**
     * Creates a new tile from a template
     *
     * @param template    the template to assign
     * @param position    the position where the anchor field of the template is positioned
     * @param orientation the orientation of the tile
     */
    public TileImpl(TileTemplate template, Position position, TileRotation orientation) {
        this.id = UUID.randomUUID().toString();
        this.type = template.getType();
        this.name = template.getName();
        this.description = template.getDescription();
        this.size = new Size(template.getSize().columns(), template.getSize().rows());
        this.gameId = template.getGameId();
        this.audioBundleName = template.getAudioBundle();
        this.audioKey = template.getAudioKey();
        this.position = new Position(position.column(), position.row());
        this.orientation = orientation;
        template.getFields().stream().forEach(f -> this.fields.add((FieldImpl) f.copy()));
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public TileRotation getRotation() {
        return orientation;
    }


    /**
     * This method changes the fields of the tile relative to the position. Assuming we have the following tile and assign it to {@link Position} (1|2) of the plan with {@link
     * TileRotation#DEGREE_0}
     * <pre>{@code
     *   ┌─────────┬─────────┬─────────┬─────────┐   ┌─────────┬─────────┐
     *   │         │         │         │         │   │         │ ███████ │
     *   │   0/4   │   1/4   │   2/4   │   3/4   │   │   0/1   │   1/1   │
     *   │         │         │         │         │   │         │ ███████ │
     *   ├─────────┼─────────┼─────────┼─────────┤   ├─────────┼─────────┤
     *   │         │         │         │         │   │         │         │
     *   │   0/3   │   1/3   │   2/3   │   3/3   │   │   0/0   │   1/0   │
     *   │         │         │         │         │   │ 0°      │         │
     *   ├─────────┼─────────┼─────────┼─────────┤   └─────────┴─────────┘
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
     * }</pre>
     * this would result in the following new coordinates for the fields of the tile
     * <pre>{@code
     *   ┌─────────┬─────────┬─────────┬─────────┐
     *   │         │         │         │         │
     *   │         │         │         │         │
     *   │         │         │         │         │
     *   ├─────────┼─────────┼─────────┼─────────┤
     *   │         │   0/1   │   1/1   │         │
     *   │         │    =    │    =    │         │
     *   │         │   1/3   │   2/3   │         │
     *   ├─────────┼─────────┼─────────┼─────────┤
     *   │         │    1/2  │   1/0   │         │
     *   │         │     =   │    =    │         │
     *   │         │ 0° 0/0  │   2/2   │         │
     *   ├─────────┼─────────┼─────────┼─────────┤
     *   │         │         │         │         │
     *   │         │         │         │         │
     *   │         │         │         │         │
     *   ├─────────┼─────────┼─────────┼─────────┤
     *   │         │         │         │         │
     *   │         │         │         │         │
     *   │         │         │         │         │
     *   └─────────┴─────────┴─────────┴─────────┘
     * }</pre>
     * but if we use {@link TileRotation#DEGREE_90} it would be
     * <pre>{@code
     *   ┌─────────┬─────────┬─────────┬─────────┐
     *   │         │         │         │         │
     *   │         │         │         │         │
     *   │         │         │         │         │
     *   ├─────────┼─────────┼─────────┼─────────┤
     *   │         │         │         │         │
     *   │         │         │         │         │
     *   │         │         │         │         │
     *   ├─────────┼─────────┼─────────┼─────────┤
     *   │         │     1/2 │   0/1   │         │
     *   │         │      =  │    =    │         │
     *   │         │ 90° 0/0 │   2/2   │         │
     *   ├─────────┼─────────┼─────────┼─────────┤
     *   │         │   1/0   │   1/1   │         │
     *   │         │    =    │    =    │         │
     *   │         │   1/1   │   2/1   │         │
     *   ├─────────┼─────────┼─────────┼─────────┤
     *   │         │         │         │         │
     *   │         │         │         │         │
     *   │         │         │         │         │
     *   └─────────┴─────────┴─────────┴─────────┘
     * }</pre>
     */
    protected void shiftAndRotate() {
        List<FieldImpl> templateFields = new ArrayList<>(this.fields);
        this.fields.clear();

        templateFields.forEach(f -> {
            switch (orientation) {
                case DEGREE_0:
                    this.fields.add(f.shift(position.column() + f.getPosition().column(), position.row() + f.getPosition().row()));
                    break;
                case DEGREE_90:
                    this.fields.add(f.shift(position.column() + f.getPosition().row(), position.row() - f.getPosition().column()));
                    break;
                case DEGREE_180:
                    this.fields.add(f.shift(position.column() - f.getPosition().column(), position.row() - f.getPosition().row()));
                    break;
                case DEGREE_270:
                    this.fields.add(f.shift(position.column() - f.getPosition().row(), position.row() + f.getPosition().column()));
                    break;
            }
        });
    }

    /**
     * Checks if this tile has a {@link Field} in the {@link #fields} list that matches the passed position.
     *
     * @param position the position to check
     *
     * @return {@code true} if the tile has a field with the given position, {@code false} otherwise.
     */
    protected boolean hasFieldPosition(Position position) {
        return fields
                .stream()
                .filter(f -> f.getPosition().equals(position))
                .collect(Collectors.toList())
                .size() != 0;
    }

    @Override
    public Tile copy() {
        TileImpl tile = new TileImpl(this, position, orientation);
        tile.id = this.id; // needed because the ID is generated in the constructor.
        return tile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TileImpl tile = (TileImpl) o;
        return Objects.equals(id, tile.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id);
    }

    @Override
    public String toString() {

        if (LOG.isTraceEnabled()) {
            return "TileImpl{" +
                           "name='" + name + '\'' +
                           ", gameId='" + gameId + '\'' +
                           ", id='" + id + '\'' +
                           ", position=" + position +
                           ", orientation=" + orientation +
                           ", type=" + type +
                           ", size=" + size +
                           ", fields=" + fields +
                           '}';
        } else {
            return "{ gameId='" + gameId + '\'' +
                           ", id='" + id + '\'' +
                           ", position=" + position +
                           '}';
        }


    }
}
