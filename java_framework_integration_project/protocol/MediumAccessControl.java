package protocol;

import client.Message;

import java.util.concurrent.BlockingQueue;

public class MediumAccessControl {
    private boolean sentPacket;

    public MediumAccessControl() {
        this.sentPacket = false;
    }

    /**
     * this function checks if we receive anything, if not we are allowed to send and return true.
     * @param receivedQueue
     * @return
     */
    public Boolean canWeSend (BlockingQueue<Message> receivedQueue, BlockingQueue<byte[]> bufferQueue) {
        if (receivedQueue.size() == 0 && bufferQueue.size() > 0) {
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
}

