package protocol;

import client.MessageType;

public class Packet {
    private int source;
    private int destination;
    private int packetType;
    private int seqNr;
    private int ackNr;
    private int dataLen;
    private byte[] data;

    public Packet() {
        data = new byte[29];
        source = 0;
        destination = 0;
        packetType = 0;
        seqNr = 0;
        dataLen = 0;
        ackNr = 0;
    }

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

    public int getDataLen() {
        return dataLen;
    }

    /**
     * Gets a number from a certain selection of bits
     *
     * @param binaryInput   Binary number to read from
     * @param numBitsToRead How many bits to read to the left of the ending position
     * @param endingPos     From 0-7, Indicates the end reading position from the right side
     * @return An integer
     */
    public int bitExtracted(int binaryInput, int numBitsToRead, int endingPos) {
        return (((1 << numBitsToRead) - 1) & (binaryInput >> (endingPos)));
    }

    /**
     * Decoding a packet and entering the details
     *
     * @param msg  The whole packet
     * @param type Message type, to account for differences in message size
     */
    public void decode(byte[] msg, MessageType type) {
        if (type == MessageType.DATA) {
            source = bitExtracted(msg[0], 4, 4);
            destination = bitExtracted(msg[0], 4, 0);
            packetType = bitExtracted(msg[1], 2, 6);
            dataLen = bitExtracted(msg[1], 6, 0);
            seqNr = msg[2];
            System.arraycopy(msg, 3, data, 0, msg.length - 3);
        } else if (type == MessageType.DATA_SHORT) {
            source = bitExtracted(msg[0], 4, 4);
            destination = bitExtracted(msg[0], 4, 0);
            ackNr = msg[1];
        }
    }

    public void setSource(int source) {
        this.source = source;
    }

    public void setDestination(int destination) {
        this.destination = destination;
    }

    public void setPacketType(int packetType) {
        this.packetType = packetType;
    }

    public void setSeqNr(int seqNr) {
        this.seqNr = seqNr;
    }

    public void setAckNr(int ackNr) {
        this.ackNr = ackNr;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setDataLen(int dataLen) {
        this.dataLen = dataLen;
    }

    /**
     * Adding the decoded stuff back in a new packet
     *
     * @param type Message type, to account for differences in message size
     * @return the new packet
     */
    public byte[] makePkt(MessageType type) {
        byte[] pkt = new byte[32];
        if (type == MessageType.DATA) {
            byte sourceDest = (byte) (source << 4 | destination);//we add the source into the byte packet and shift it four bits to add the
            byte typeAndData = (byte) (packetType << 6 | dataLen);
            pkt[0] = sourceDest;
            pkt[1] = typeAndData;
            pkt[2] = (byte) seqNr;
            int j = 0;
            int len;
            if (getDataLen() < 29) {
                len = getDataLen() + 3;
            } else {
                len = 32;
            }
            for (int i = 3; i < len; i++) {
                pkt[i] = data[j];
                j++;
            }
        }
        if (type == MessageType.DATA_SHORT) {
            pkt = new byte[2];
            byte sourceDest = (byte) (source << 4 | destination);
            pkt[0] = sourceDest;
            pkt[1] = (byte) ackNr;
        }
        return pkt;
    }
}
