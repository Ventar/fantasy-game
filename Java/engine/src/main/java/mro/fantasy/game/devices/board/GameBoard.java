package mro.fantasy.game.devices.board;

import mro.fantasy.game.engine.events.BoardUpdatedEvent;
import mro.fantasy.game.engine.events.GameEventListener;
import mro.fantasy.game.engine.events.GameEventProducer;

import java.util.List;

/**
 * The logical game board that is used to display a plan. A game board consist of multiple {@link BoardModule} which are put together to build a larger game board to use. The
 * implementing class is responsible for the translation of the coordinate system between the individual board modules with their rows and columns and the coordinate system of the
 * game board. The following diagramm shows two board modules which are aligned to build a larger game board. As you can see they both have their own coordinate system for their
 * columns and rows.
 * <pre>{@code
 *   ┌─────────┬─────────┬─────────┐┌─────────┬─────────┬─────────┐
 *   │         │         │         ││         │         │         │
 *   │   0/2   │   1/2   │   2/1   ││   0/2   │   1/2   │   2/1   │
 *   │         │         │         ││         │         │         │
 *   ├─────────┼─────────┼─────────┤├─────────┼─────────┼─────────┤
 *   │         │         │         ││         │         │         │
 *   │   0/1   │   1/1   │   2/1   ││   0/1   │   1/1   │   2/1   │
 *   │         │         │         ││         │         │         │
 *   ├─────────┼─────────┼─────────┤├─────────┼─────────┼─────────┤
 *   │         │         │         ││         │         │         │
 *   │   0/0   │   1/0   │   2/0   ││   0/0   │   1/0   │   2/0   │
 *   │         │         │         ││         │         │         │
 *   └─────────┴─────────┴─────────┘└─────────┴─────────┴─────────┘
 *     Module A                       Module B
 * }</pre>
 * The purpose of the implementation is to hide the individual boards and create a single logical one:
 * <pre>{@code
 *   ┌─────────┬─────────┬─────────┐┌─────────┬─────────┬─────────┐
 *   │         │         │         ││         │         │         │
 *   │   0/2   │   1/2   │   2/1   ││   3/2   │   4/2   │   5/1   │
 *   │         │         │         ││         │         │         │
 *   ├─────────┼─────────┼─────────┤├─────────┼─────────┼─────────┤
 *   │         │         │         ││         │         │         │
 *   │   0/1   │   1/1   │   2/1   ││   3/1   │   4/1   │   5/1   │
 *   │         │         │         ││         │         │         │
 *   ├─────────┼─────────┼─────────┤├─────────┼─────────┼─────────┤
 *   │         │         │         ││         │         │         │
 *   │   0/0   │   1/0   │   2/0   ││   3/0   │   4/0   │   5/0   │
 *   │         │         │         ││         │         │         │
 *   └─────────┴─────────┴─────────┘└─────────┴─────────┴─────────┘
 *      Game Board
 * }</pre>
 * If an action is triggered now that would set the LED color of field (3/1) to red, the internal implementation has to translate that to Module B (0/1). Same for the other way
 * around when events from the modules are received. s *
 *
 * @author Michael Rodenbuecher
 * @since 2022-11-18
 */
public interface GameBoard extends BoardModule, GameEventProducer<BoardUpdatedEvent, GameEventListener<BoardUpdatedEvent>> {


    /**
     * Performs the setup of the board with the given board modules. The setup will connect the passed modules in a way that a larger module is formed.
     *
     * @param modules  the physical modules which are used to build the game board
     * @param colorize if set to {@code true} the edges of the modules are colorized so that you can put them together in the right way
     */
    void setup(List<BoardModule> modules, boolean colorize);


}
