/**
 * Package to handle events which are sent to the game server by any device.
 *
 * <h2>Device Event Processing</h2>
 * The processing of the events is done by two kind of services, the {@link mro.fantasy.game.devices.events.DeviceEventService} which is able to receive {@link
 * mro.fantasy.game.devices.events.DeviceDataPackage} from hardware devices, and the {@link mro.fantasy.game.engine.events.GameEventProducer} which will convert these events to
 * internal {@link mro.fantasy.game.engine.events.GameEvent}s and forward them to {@link mro.fantasy.game.engine.events.GameEventListener}. The producer itself is triggered by
 * the {@code DeviceEventService} whenever an event arrives from a device. To achive this it implements the @{@link mro.fantasy.game.devices.events.DeviceEventHandler} interface.
 * <p>
 * <img src="doc-files/DeviceEventProcessing.png"/>
 *
 * <h2>Handler vs. Listener in the Context of Spring</h2>
 * The event implementation uses handler and listeners. Actually both types behave similar but the control flow is different. Regardless if you have a handler or a listener, both
 * of them are defined in interfaces which have a method to react on certain kind of data passed to it. In the event implementation the {@link
 * mro.fantasy.game.devices.events.DeviceEventHandler#handle(DeviceDataPackage)} method and the {@link mro.fantasy.game.engine.events.GameEventListener#onEvent(GameEvent)}
 * method do actually the same.
 * <p>
 * <img src="doc-files/HandlerVsListener.png"/>
 * <p>
 * In the using classes you can see that the listener has to be registered at a class which will inform it about changes. To achieve this, methods to register and maybe unregister
 * (or add / remove) are needed so that other classes can pass the listener to the handling class. The complete code is understandable without the usage of Spring injection,
 * however, if you have a Java class that implements the interface you need to get access to the class that provides the method to register.
 * <pre>{@code
 *   public class A implements GameEventListener<GameBoardEvent> {
 *
 *      @Autowired
 *      private GameBoard gameBoard;
 *
 *      @PostConstruct
 *      private void postConstruct() {
 *         gameBoard.register(this);
 *      }
 *
 *      @Override
 *      public void onEvent(GameBoardEvent event) {
 *         ...
 *      }
 *   }
 * }</pre>
 * <p>
 * If a handler is used on the other hand it will load the classes which implement the {@link mro.fantasy.game.devices.events.DeviceEventHandler} interface when the bean is
 * created:
 * <pre>{@code
 *  public class DeviceEventService {
 *
 *      @Autowired
 *      private List<DeveiceEventHandler> eventHandler;
 *  }
 * }</pre>
 * <p>
 * Both concepts are valid and will work. The major difference is that the handler implementation is static and will only be autowired during the startup of the implementation. The
 * advantage is the reduced implementation effort and less needed interfaces. However, it is lacking the flexibility of the listener implementation which can change the behaviour
 * during runtime:
 * <pre>{@code
 *  public class A  {
 *
 *      @Autowired
 *      private GameBoard gameBoard;
 *
 *      public void doSomething(...) {
 *         gameBoard.registerListener(new GameEventListener() { ... });
 *      }
 *   }
 * }</pre>
 *
 * <h2>Registration and Incoming Event Flow</h2>
 * The following diagramm shows the registration and the processing of incoming device events in a flow overview.
 * <img src="doc-files/IncomingDeviceEvent.png"/>
 *
 * <h2>Waiting for Events</h2>
 * The {@link mro.fantasy.game.engine.events.GameEventProducer#waitForEvent()} method offers the possibility to wait for a certain event. This mechanism is frequently used within
 * the game engine when the execution is stopped to allow the players to interact with the hardware (board or player controller for example).
 */
package mro.fantasy.game.devices.events;

import mro.fantasy.game.engine.events.GameEvent;