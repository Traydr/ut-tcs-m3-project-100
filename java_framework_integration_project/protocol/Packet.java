package protocol;

import client.MessageType;

import java.math.BigInteger;

public class Packet {
    private int source;
    private int destination;
    private int packetType;
    private int seqNr;
    private int ackNr;
    private byte[] data;

    public int getAckNr() {
        return ackNr;
    }

    public int getSource() {
        return source;
    }

    public int getDestination() {
        return destination;
    }

    public int getPacketType() {
        return packetType;
    }

    public int getSeqNr() {
        return seqNr;
    }

    public byte[] getData() {
        return data;
    }

    /**
     * Gets a number from a certain num of bits
     * @param binNum Number to extract from
     * @param numBitsShifted How many bits to read
     * @param startingPos From 1-8, From the left side of the byte
     * @return
     */
    public int bitExtracted(int binNum, int numBitsShifted, int startingPos){
        return (((1 << numBitsShifted) - 1) & (binNum >> (startingPos - 1)));
    }

    /**
     * Decoding a packet and entering the details
     * @param msg The whole packet
     */
    public void decode(byte[] msg, MessageType type){
        if (type == MessageType.DATA) {
            source = bitExtracted(msg[0], 4, 0);
            destination = bitExtracted(msg[0], 4, 4);
            packetType = bitExtracted(msg[1], 2, 0);
            seqNr = msg[2];
        } else if (type == MessageType.DATA_SHORT) {
            source = bitExtracted(msg[0], 4, 0);
            destination = bitExtracted(msg[0], 4, 4);
            ackNr = msg[1];
        }

    }
}
