/**
 * This package manages the aspects of the physical electronic board modules and combine them to a logical {@link mro.fantasy.game.devices.board.GameBoard}.
 * <h1>Electronic Components</h1>
 * <h2>Game Board</h2>
 * <h3>Fields</h3>
 * A physical device consists of multiple field that can be used to display the current status of the game. Every field has the following capabilities
 * <ul>
 *     <li>a switch button that can be pressed by the player to select that field</li>
 *     <li>an LED that can display one of the predefined {@link mro.fantasy.game.devices.impl.Color}s of the game board</li>
 *     <li>one Board sensor, that is able to detect if a floor plate was positioned on that field. Floor plates can cover one or more fields and indicate to the players that
 *     these fields are part of the current quest. </li>
 *     <li>four edge sensors that can be used to check if a {@link mro.fantasy.game.plan.Tile} (which can be a scenic one or a character for example) was placed on top of the
 *     floor plate and which direction the tile has</li>
 * </ul>
 *  Field Example
 * <pre>{@code
 *     BOARD
 *     |
 *    ┌─────────────────────┐
 *    │ █      NORTH        │
 *    │         ███         │
 *    │ W                 E │
 *    │ E █             █ A │
 *    │ S █             █ S │
 *    │ T                 T │
 *    │         ███         │
 *    │        SOUTH        │
 *    └─────────────────────┘
 *  }</pre>
 *  <h3>Sectors</h3>
 * <pre>{@code
 *   ┌─────────┬─────────┐
 *   │    I    │    M    │
 *   │ L     J │ P     N │
 *   │    K    │    O    │
 *   ├─────────┼─────────┤
 *   │    A    │    E    │
 *   │ D     B │ H     F │
 *   │    C    │    G    │
 *   └─────────┴─────────┘
 * }</pre>
 *  Four of these fields are grouped to a sector that is responsible for the edge sensors in that area. The reason is the so called PCA9555 multiplexer; an electronic component
 *  that can aggregate the sensor states of all edge sensors and encode them in two bytes. These multiplexers have individual addresses that allow to identify the state of
 *  every sensor based on the address and the position in the bytes:
 * <p>
 *  Bit    | 7 6 5 4 3 2 1 0 | 7 6 5 4 3 2 1 0 |
 *  Sensor | H G F E D C B A | P O N M L K J I |
 * <p>
 * What is missing here is the state of the button sensors and the board sensors. To reduce the needed number of electronic components 4 ( 0 - 3 in the picture below)of the
 * sectors are combined in a submodule which has two additional PCA9555 components to track the state of the board sensors and the buttons.
 * <h2> Submodules </h2>
 * From an electronic perspective the sectors are combined to electronic submodules. A submodule consists of 16 fields, i.e. it has 16 buttons, 16 LEDS, 16 board sensors and 64
 * edge sensors. As described before the state of the edge sensors is managed on sector level, but the state of the buttons and board sensors is managed on submodule level. The
 * following image shows the sectors with their addresses and the position in on the submodule:
 * <pre>{@code
 *   Board Sensors
 *   ┌─────────┬─────────┬─────────┬─────────┐
 *   │ O       │ P       │ K       │ L       │
 *   │         2         │         3         │
 *   ├────    0x23   ────┼─────   0x24  ─────┤
 *   │ M         N       │ I         J       │
 *   │         │         │         │         │
 *   ├─────────┼─────────┼─────────┼─────────┤
 *   │ C       │ D       │ G       │ H       │
 *   │         0         │         1         │
 *   ├────    0x21   ────┼────    0x22   ────┤
 *   │ A         B       │ E         F       │
 *   │         │         │         │         │
 *   └─────────┴─────────┴─────────┴─────────┘
 * }</pre>
 * <pre>{@code
 *   Buttons
 *   ┌─────────┬─────────┬─────────┬─────────┐
 *   │ K       │ L       │ O       │ P       │
 *   │         2         │         3         │
 *   ├────    0x23   ────┼─────   0x24  ─────┤
 *   │ I         J       │ M         N       │
 *   │         │         │         │         │
 *   ├─────────┼─────────┼─────────┼─────────┤
 *   │ C       │ D       │ G       │ H       │
 *   │         0         │         1         │
 *   ├────    0x21   ────┼────    0x22   ────┤
 *   │ A         B       │ E         F       │
 *   │         │         │         │         │
 *   └─────────┴─────────┴─────────┴─────────┘
 * }</pre>
 * In addition to the multiplexers for the edge sensors the multiplexers with address 0x20 and 0x24 are responsible for the pressed buttons and the board sensors. As you can see
 * each multiplexer is able to handle 16 sensors. The order looks a little bit random, which is the case due to the routing on the PCBs. As for the sensors the pressed buttons
 * and board can be encoded in 2 bytes:
 *
 * <p>
 * To make the orientation in the coordinate system of a submodule easier (and in the coordinate system of a complete board module) we can assign each of these fields  an
 * individual address that consists of the column and the row of the field in the context of the module:
 * <pre>{@code
 *   ┌─────────┬─────────┬─────────┬─────────┐
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
 * }</pre>
 * <h3>Board Modules</h3>
 * Finally, a complete {@link mro.fantasy.game.devices.board.impl.BoardModuleImpl} consists of 4 submodules so that a board module has a size of 8x8 fields. The 4 submodules are
 * named with a letter, i.e. A / B / C / D and are ordered in the following way:
 * <pre>{@code
 *   ┌─────────┬─────────┐
 *   │         │         │
 *   │    C    │    D    │
 *   │         │         │
 *   ├─────────┼─────────┤
 *   │         │         │
 *   │    A    │    B    │
 *   │         │         │
 *   └─────────┴─────────┘
 * }</pre>
 * <h2>Encoding</h2>
 * If we put all this information together, a board module has 8 columns and 8 rows which leads to
 * <ul>
 *     <li>64 Buttons</li>
 *     <li>64 Board Sensors</li>
 *     <li>256 Edge Sensors</li>
 *     <li>64 LEDs</li>
 * </ul>
 * When data is sent this game engine, this information needs to be encoded. Since all of these sensors and buttons have exactly 2 states (button pressed / not pressed, sensor
 * magnetic field detected / not detected) a single bit can represent the current state of a button or sensor. The LEDs cannot detect anything and will therefor not trigger any
 * events. As a result 384 states need to be managed. A single byte can hold information about 8 states so that the complete state of a board module with 64 fields will fit into
 * 48 bytes. To handle this information the concrete order of these bytes and the mapping to the fields we described above is important. You can find the details on that in the
 * description of the {@link mro.fantasy.game.devices.events.DeviceMessageType}.
 */
package mro.fantasy.game.devices.board;

