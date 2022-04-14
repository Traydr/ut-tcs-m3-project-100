package protocol;

public class Forwarding {

    final int NODE_COUNT = 4;
    int myAddress;
    int[] sources = new int[4];
    int c = 0;
    int counter;
    int[][] dataTable = new int[NODE_COUNT][NODE_COUNT];

    public Forwarding(int node) {
        myAddress = node;
        sources[c] = node;
        c++;
    }

    public void init(int neighbour, int step) {
        dataTable[c][neighbour] = 1;
        if (step > counter){
            // TODO update datatable with new data
        } else {

        }
    }

    public void pathFinding(int[][] forwardingTable) {
        for (int k = 0; k < NODE_COUNT; k++) {
            for (int i = 0; i < NODE_COUNT; i++) {
                for (int j = 0; j < NODE_COUNT; j++) {
                    if (i != j && forwardingTable[i][j] == 0) {
                        forwardingTable[i][j] = forwardingTable[i][k] * forwardingTable[k][j];
                    }
                }
            }
        }
    }

    public byte[] matrixToArray() {
        byte[] pkt = new byte[NODE_COUNT * NODE_COUNT];
        int count = 0;
        for (int i = 0; i < NODE_COUNT; i++) {
            for (int j = 0; j < NODE_COUNT; j++) {
                pkt[count] = (byte) dataTable[i][j];
                count++;
            }
        }
        return pkt;
    }

    public int[][] arrayToMatrix(Packet pck) {
        byte[] data = pck.getData();
        int[][] dataMatrix = new int[NODE_COUNT][NODE_COUNT];
        for (int i = 0; i < NODE_COUNT; i++) {
            for (int j = 0; j < NODE_COUNT; j++) {
                dataMatrix[i][j] = data[4 * i + j];
            }
        }

        return dataMatrix;
    }


}

