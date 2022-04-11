import client.MessageType;

import java.util.Random;

public class MediumAccessControl {
    private static final int MAX_TIMEOUT = 6;
    private static final int IDLE_MULTIPLIER = 10;
    private boolean areWeSending = false;
    private boolean wasLastSend = false;
    private boolean sentRTS = false;
    private int idleCounter = 0;
    private int packetsToSend = 0;
    private int wasLastSendCounter = 0;

    public MessageType areWeSending (MessageType previousMediumState, int localQueueLength) {
        if (previousMediumState == MessageType.FREE && localQueueLength == 0) {
            idleCounter ++;
        } else {
            idleCounter = 0;
        }

        if (MAX_TIMEOUT == wasLastSendCounter) {
            wasLastSend = false;
            wasLastSendCounter = 0;
        } else if (wasLastSend) {
            wasLastSendCounter ++;
            return MessageType.FREE;
        }

        if (localQueueLength == 0) {
            return MessageType.FREE;
        }

        if (packetsToSend == 1 && areWeSending) {
            areWeSending = false;
            sentRTS = false;
            packetsToSend--;
            wasLastSend = true;
            return MessageType.SENDING;
        }

        if (areWeSending) {
            packetsToSend--;
            return MessageType.SENDING;
        }
/**
        if (previousMediumState == MessageType.FREE && new Random().nextInt(100) < 37 + IDLE_MULTIPLIER * idleCounter) {
            sentRTS = true;
            return MessageType.SENDING;
        }

        if (sentRTS && previousMediumState == MessageType.FREE) {
            sentRTS = false;
        }
**/


        return previousMediumState;
    }
}
