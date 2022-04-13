package protocol;

public class Forwarding {

    final int NODE_COUNT = 4;
    int myAddress;
    int[][] dataTable = new int[NODE_COUNT][NODE_COUNT];

    public Forwarding(int node) {
        myAddress = node;
    }

    public void init(int[][] forwardingTable) {
        for (int i = 0; i < NODE_COUNT; i++) {
            System.arraycopy(forwardingTable[i], 0, dataTable[i], 0, NODE_COUNT);
        }
    }

    public void pathFinding(int[][] forwardingTable) {
        for (int k = 0; k < NODE_COUNT; ++k) {
            for (int i = 0; i < NODE_COUNT; ++i) {
                for (int j = 0; j < NODE_COUNT; ++j) {
                    if (i != j && forwardingTable[i][j] == 0) {
                        forwardingTable[i][j] = forwardingTable[i][k] * forwardingTable[k][j];
                    }
                }
            }
        }
    }

    public byte[] matrixToArray() {
        byte[] pkt = new byte[16];
        int count = 0;
        for (int i = 0; i < NODE_COUNT; i++) {
            for (int j = 0; j < NODE_COUNT; j++) {
                pkt[count] = (byte) dataTable[i][j];
                count++;
            }
        }
        return pkt;
    }


}

