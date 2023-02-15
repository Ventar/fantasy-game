/**
 * Package to handle events which are generated by components of the server.
 *
 * <h2>Game Event Processing Background</h2>
 * The processing of events is an async process that is usually triggered through an action that was performed by a player. This happens when a player uses the RFID reader to read
 * a tag or moves a miniature on the game board. This can happen at any time of the programm execution and in most of the cases the server ha to react on these actions.
 * <p>
 * There is a differentiation between events that
 * <ol>
 *     <li>need to happen to progress in the game and are expected by the server (player has to decide which action he takes, a tile has to be placed during the game board
 *     setup, an enemy must be removed from the board field</li>
 *     <li>happen in an async way and do not affect the current phase of the game (a player can consume a health potion at any time or change his weapons)</li>
 *     <li>occur unexpected and need a reaction from the game engine (a miniature was removed from the board although the server did not ask to do that)</li>
 * </ol>
 * Depending on the case the server / engine has to perform different tasks. In general the flow of a game session is linear, i.e. the server (which controls the enemies) and
 * the player interact with each other in an order defined by their initiative. However, due to the nature of the game it is possible for the players to perform actions in the
 * real world which are not expected by the engine. If a player for example removes an enemy from the game board at a point where the engine does not expect it, it has to react
 * on that. This is covered by example three in the list above and would interrupt the current flow of the game. As a result the engine needs to communicate with the player and
 * ensures that everything is in a consistent state before the game can progress.
 * <p>
 * The second situation will happen when a player performs an action that has no effect on the current order of the game. Players have a certain amount of energy they can use to
 * perform actions. Some of these actions can only be performed during the turn of a player, others may be executed outside. Every action of a player has to be paid with this
 * energy and if the player performs them outside his turn, this energy is reduced.The recovery will only happen during his turn.
 * The advantage is that a player can perform preparation work for his next turn without interrupting other players. Examples are the consumption of potions or the change of
 * weapons. This is covered by number two.
 * <p>
 * Finally, during the regular flow of the game, the engine has to wait for player interaction or for the execution of command regarding the game setup.
 * <p>
 * <h2>Game Event Processing Technical</h2>
 * <img src="doc-files/EngineEventOverview.png"/>
 * <p>
 * From a technical perspective event in which the engine is interested are generated by {@link mro.fantasy.game.engine.events.GameEventProducer} implementations. Usually
 * all device types (boards, player controller, game controller) have their own producer implementation that handles the device events at the same time. The implementation
 * is responsible for the conversion from an incoming device message to an event the engine can handle.
 * <p>
 * The engine uses {@link mro.fantasy.game.engine.events.GameEventListener} which are registered at the producer to define actions that should be executed when a new event
 * arrives. In the previous chapter the different types of interactions were described. For all async operations a listener is responsible but in case the engine is waiting
 * for a player interaction, it uses the {@link mro.fantasy.game.engine.events.GameEventProducer#waitForEvent()} method to make the async event processing synchron. Using
 * that method will allow the engine to wait for the next event.
 * <p>
 * The event itself is always of type {@link mro.fantasy.game.engine.events.GameEvent} but will usually have an extending interface that offers more information about the event.
 * The {@link mro.fantasy.game.engine.events.BoardUpdatedEvent} for example informs the engine about a change of the HAL sensors on the
 * {@link mro.fantasy.game.devices.board.GameBoard}.
 * <p>
 * To make the implementation of the producer easier an abstract base class {@link mro.fantasy.game.engine.events.impl.AbstractGameEventProducer} is provided that can be used by
 * the implementing classes to use predefined implementations for the registration of listeners and the functionality to wait for the next event.
 *
 * @author Michael Rodenbuecher
 * @since 2022-11-26
 */
package mro.fantasy.game.engine.events;