package protocol;

import java.util.HashMap;

public class ReliableTransfer {
    public boolean hasReceived(HashMap<Integer, HashMap<Integer, Packet>> receivedPackets) {
        if(receivedPackets.size() > 0) {
            return true;
        }
        return false;
    }
}
