package mro.fantasy.game.plan;

import java.util.List;
import java.util.Map;

/**
 * Calculates the delta between two plans.
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-12
 */
public interface PlanDeltaService {


    /**
     * The type of change that happened from one version of the plan to the next.
     *
     * @author Michael Rodenbuecher
     * @since 2022-08-07
     */
    enum ChangeType {
        /**
         * A {@link Tile} was added to the plan
         */
        TILE_ADDED,
        /**
         * A {@link Tile} was removed from the plan
         */
        TILE_REMOVED,
        /**
         * A {@link Tile} has changed its position (which is similar to remove the tile and put it back at a different position)
         */
        TILE_MOVED
    }


    /**
     * Calculates a change between the original and the changed plan. There are different {@link ChangeType}s which are checked. By comparing the {@link Tile#getId()} added and
     * removed tiles can be detected. If the changed plan has a tile with an ID that is not in the original plan it was {@link ChangeType#TILE_ADDED}, if the original plan has a
     * tile with an ID that is not in the changed plan it was {@link ChangeType#TILE_REMOVED}. For the {@link ChangeType#TILE_MOVED} check the {@link Tile#getPosition()} of the
     * tiles with the same ID is compared between the original and the changed plan tiles. If the position has changed the tile was moved.
     *
     * @param original the original plan
     * @param changed  the changed plan
     *
     * @return the delta between original and changed.
     */
    Map<ChangeType, List<Tile>> calculateChange(Plan original, Plan changed);
}
