package mro.fantasy.game.devices.player;

import mro.fantasy.game.engine.events.GameEvent;
import mro.fantasy.game.engine.events.GameEventListener;

/**
 * Physical controller of a player.
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-12
 */
public interface PlayerController extends GameEventListener<GameEvent> {
}
