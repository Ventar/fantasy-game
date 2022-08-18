package mro.fantasy.game.devices.board.impl;

import mro.fantasy.game.devices.DeviceType;
import mro.fantasy.game.devices.board.BoardController;
import mro.fantasy.game.devices.board.BoardControllerEventListener;
import mro.fantasy.game.devices.events.DeviceDataPackage;
import mro.fantasy.game.devices.events.impl.AbstractGameEventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BoardControllerImpl extends AbstractGameEventProducer<BoardControllerEventListener.BoardEvent, BoardControllerEventListener> implements BoardController {

    /**
     * Logger.
     */
    public static final Logger LOG = LoggerFactory.getLogger(BoardControllerImpl.class);

    public BoardControllerImpl() {
        super(DeviceType.BOARD_MODULE);
    }

    @Override
    public BoardControllerEventListener.BoardEvent createEvent(DeviceDataPackage eventData) {
        return new BoardControllerEventListener.BoardEvent(this, null);
    }
}
