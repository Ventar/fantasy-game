package mro.fantasy.game.devices.board.impl;

import mro.fantasy.game.devices.DeviceType;
import mro.fantasy.game.devices.board.GameBoard;
import mro.fantasy.game.devices.board.GameBoardEventListener;
import mro.fantasy.game.devices.events.DeviceDataPackage;
import mro.fantasy.game.devices.events.impl.AbstractGameEventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GameBoardImpl extends AbstractGameEventProducer<GameBoardEventListener.GameBoardEvent, GameBoardEventListener> implements GameBoard {

    /**
     * Logger.
     */
    public static final Logger LOG = LoggerFactory.getLogger(GameBoardImpl.class);

    public GameBoardImpl() {
        super(DeviceType.BOARD_MODULE);
    }

    @Override
    public GameBoardEventListener.GameBoardEvent createEvent(DeviceDataPackage eventData) {
        return new GameBoardEventListener.GameBoardEvent(this, null);
    }
}
