package protocol;

import client.Message;
import client.MessageType;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

public class MediumAccessControl {
    private boolean sentPacket;
    private MessageType previousMediumState;
    private ArrayList<MessageType> sendingTypes;

    public MediumAccessControl() {
        this.sentPacket = false;
        this.previousMediumState = MessageType.FREE;
        this.sendingTypes = new ArrayList<>();
        sendingTypes.add(MessageType.FREE);
        sendingTypes.add(MessageType.DONE_SENDING);
        sendingTypes.add(MessageType.HELLO);
    }

    /**
     * this function checks if we receive anything, if not we are allowed to send and return true.
     * @param receivedQueue
     * @return True if we can send, otherwise false
     */
    public Boolean canWeSend (BlockingQueue<Message> receivedQueue, BlockingQueue<byte[]> bufferQueue) {
        return receivedQueue.size() == 0 && bufferQueue.size() > 0 && sendingTypes.contains(previousMediumState);
    }

    public boolean isSentPacket() {
        return sentPacket;
    }

    public void haveSentPacket() {
        this.sentPacket = true;
    }

    public void setPreviousMediumState(MessageType previousMediumState) {
        this.previousMediumState = previousMediumState;
    }
}

