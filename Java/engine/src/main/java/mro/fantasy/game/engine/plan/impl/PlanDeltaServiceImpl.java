package mro.fantasy.game.engine.plan.impl;

import mro.fantasy.game.engine.plan.PlanDeltaService;
import mro.fantasy.game.engine.plan.Plan;
import mro.fantasy.game.engine.plan.Tile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Calculates the delta between two plans.
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-12
 */
@Service
public class PlanDeltaServiceImpl implements PlanDeltaService {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(PlanDeltaServiceImpl.class);

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
    public Map<ChangeType, List<Tile>> calculateChange(Plan original, Plan changed) {

        LOG.debug("Start delta calculation for plan :.= [{}]", original.getName());

        var result = new HashMap<ChangeType, List<Tile>>();
        Arrays.stream(ChangeType.values()).forEach(ct -> result.put(ct, new ArrayList<>()));  // initialize the result map

        // Check if a tile was added. We remove all tiles from the changed list that are in the original list. The remaining elements are those which were added.
        var addedTiles = changed.getTiles();
        addedTiles.removeAll(original.getTiles());
        addedTiles.forEach(t -> result.get(ChangeType.TILE_ADDED).add(t));

        // Perform the operation with switched lists to get the removed tiles.
        var removedTiles = original.getTiles();
        removedTiles.removeAll(changed.getTiles());
        removedTiles.forEach(t -> result.get(ChangeType.TILE_REMOVED).add(t));

        // figure out the position changes is a little more complex because we need to compare the position of the tiles.
        changed.getTiles().stream()
                .filter(cTile -> {
                    var origTile = original.getTileById(cTile.getId());                         // try to fetch the same tile in the original list
                    return origTile != null && !origTile.getPosition().equals(cTile.getPosition());  // if the tile was added we will not find it, so it has obviously not moved
                })                                                                                   // otherwise, we compare the position
                .forEach(t -> result.get(ChangeType.TILE_MOVED).add(t));                             // and add the moved tiles to the result

        LOG.debug("Start delta calculation for plan :.= [{}], added ::= [{}], removed ::= [{}], moved ::= [{}]", original.getName(), result.get(ChangeType.TILE_ADDED),
                result.get(ChangeType.TILE_REMOVED), result.get(ChangeType.TILE_MOVED));

        return result;
    }

}
