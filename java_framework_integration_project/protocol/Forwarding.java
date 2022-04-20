package protocol;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.stream.IntStream;

public class Forwarding {

    final int NODE_COUNT = 5;
    Node myAddress;
    ArrayList<Node> sources = new ArrayList<>();
    int counter;
    int[][] dataTable = new int[NODE_COUNT][NODE_COUNT];

    public Forwarding(Node node) {
     sources.add(node);
     myAddress = node;
    }

    /**
     * Creates a table that contains information about relations between nodes
     * also it updates the current table with new data if it is the case
     *
     * if dataTable[i][j] == 1 there is a direct path between i and j
     * else if dataTable[i][j] == 0 nodes i and j are not linked
     *
     * @param neighbour
     * @param pkt
     */
    public void init(Node neighbour, Packet pkt) {
        int[][] newData = arrayToMatrix(pkt);
        dataTable[sources.indexOf(myAddress) + 1][sources.indexOf(neighbour) + 1] = 1;

        for (int i = 1; i < NODE_COUNT; i++) {
            if (dataTable[i][0] < newData[i][0]){
                System.arraycopy(newData[i], 1, dataTable[i], 1, NODE_COUNT - 1);
            }
        }


    }

    /**
     * Adds a control element to column 0
     *
     * @param step
     */
    public void addStep (int step){
        dataTable[sources.indexOf(myAddress)][0] = step;
    }

    /**
     * Computes the path Matrix, containing the necessary data for routing
     * @param forwardingTable
     */
    public void pathFinding(int[][] forwardingTable) {
        for (int k = 1; k < NODE_COUNT; k++) {
            for (int i = 1; i < NODE_COUNT; i++) {
                for (int j = 1; j < NODE_COUNT; j++) {
                    if (i != j && forwardingTable[i][j] == 0) {
                        forwardingTable[i][j] = forwardingTable[i][k] * forwardingTable[k][j];
                    }
                }
            }
        }
    }

    /**
     * Codes the dataTable in an array of bytes, to be included in the packet
     *
     * @return The byte array
     */
    public byte[] matrixToArray() {
        byte[] pkt = new byte[NODE_COUNT * NODE_COUNT];
        int count = 0;
        for (int i = 1; i < NODE_COUNT; i++) {
            for (int j = 1; j < NODE_COUNT; j++) {
                pkt[count] = (byte) dataTable[i][j];
                count++;
            }
        }
        return pkt;
    }

    /**
     * Decodes the incoming packet
     *
     * @param pck
     * @return the dataMatrix of the packet
     */
    public int[][] arrayToMatrix(Packet pck) {
        byte[] data = pck.getData();
        int[][] dataMatrix = new int[NODE_COUNT][NODE_COUNT];
        for (int i = 1; i < NODE_COUNT; i++) {
            for (int j = 1; j < NODE_COUNT; j++) {
                dataMatrix[i][j] = data[4 * i + j];
            }
        }

        return dataMatrix;
    }


}

