package mro.fantasy.game.engine.character.impl;

import mro.fantasy.game.engine.GameLibrary;
import mro.fantasy.game.engine.character.Character;
import mro.fantasy.game.engine.character.CharacterExtension;
import mro.fantasy.game.Size;
import mro.fantasy.game.engine.communication.impl.AbstractAudioGameResource;
import mro.fantasy.game.engine.gamedata.GameDataSet;

import java.util.Map;

/**
 * Implementation of the character Interface
 */
public class CharacterImpl extends AbstractAudioGameResource implements Character {

    /**
     * The basic attributes of a character without any modifications. These are the values loaded from the YAML map without any {@link CharacterExtension}s applied.
     */
    private GameDataSet basicAttributes = new GameDataSet();

    private Size size;


    @Override
    public GameDataSet getGameDataSet() {
        return null;
    }

    @Override
    public void addCharacterExtension(CharacterExtension extension) {

    }

    @Override
    public void loadFromYAML(GameLibrary library, Map<String, Object> data) {
        super.loadFromYAML(library, data);


    }

}
