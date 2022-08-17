package mro.fantasy.game.devices.plaver;

import java.util.ArrayList;
import java.util.List;

import static mro.fantasy.game.devices.plaver.RFIDCodeType.*;

public enum RFIDCode {

    TEST(TEST_TYPE, 0),
    SETUP_MODE_TUTORIAL(SETUP_MODE, 1),
    SETUP_MODE_DEFAULT(SETUP_MODE, 2),
    SETUP_MODE_FAST(SETUP_MODE, 3),
    QUEST_TUTORIAL(QUEST, 4),
    QUEST_PORTAL(QUEST, 5),
    QUEST_PLAIN(QUEST, 6),
    RISKAR(PLAYER, 10),
    LYRA(PLAYER, 11),
    GORM(PLAYER, 12),
    TIBOR(PLAYER, 13),

    ;

    private RFIDCodeType type;

    private int code;

    RFIDCode(RFIDCodeType type, int code) {
        this.type = type;
        this.code = code;
    }

    /**
     * Returns the unique code ID.
     *
     * @return the code ID.
     */
    public int getCode() {
        return code;
    }

    /**
     * Returns the group to which the RFIG code belongs
     *
     * @return the type
     */
    public RFIDCodeType getType() {
        return type;
    }


    /**
     * Returns the group to which the RFIG code belongs
     *
     * @return the type
     */
    public static List<RFIDCode> getByType(RFIDCodeType type) {
        var res = new ArrayList<RFIDCode>();

        for (RFIDCode code : RFIDCode.values()) {
            if (code.getType() == type) {
                res.add(code);
            }
        }

        return res;
    }

    /**
     * Resolves the RFID code with the given code ID.
     *
     * @param code the code id
     * @return the RFID enum
     */
    public static RFIDCode fromCode(int code) {
        for (RFIDCode b : values()) {
            if (b.getCode() == code) {
                return b;
            }
        }
        throw new IllegalArgumentException("An enum with code ::= [" + code + "] does not exist.");
    }

    @Override
    public String toString() {
        return name() + " (" + code + ")";
    }
}
