package protocol;

import client.Message;
import client.MessageType;

import java.util.concurrent.BlockingQueue;

public class MediumAccessControl {
    private boolean currentlySending;
    private boolean sentPacket;
    private MessageType previousMediumState;

    public MediumAccessControl() {
        this.currentlySending = false;
        this.sentPacket = false;
        this.previousMediumState = MessageType.FREE;
    }

    /**
     * this function checks if we receive anything, if not we are allowed to send and return true.
     * @param receivedQueue
     * @return True if we can send, otherwise false
     */
    public Boolean canWeSend (BlockingQueue<Message> receivedQueue, BlockingQueue<byte[]> bufferQueue) {
        if (receivedQueue.size() == 0 && bufferQueue.size() > 0 && previousMediumState != MessageType.BUSY) {
            return true;
        }
        return false;
    }

    public Boolean receivedMSG (BlockingQueue<Message> receivedQueue) {
        if (receivedQueue.size() > 0) {
            return true;
        }
        return false;
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

    public void setCurrentlySending(boolean currentlySending) {
        this.currentlySending = currentlySending;
    }
}

