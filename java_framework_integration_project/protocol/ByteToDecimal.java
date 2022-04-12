package protocol;

import java.math.BigInteger;

public class ByteToDecimal{
    int source;
    int destination;
    int packetType;
    int seqNr;

    public int bitExtracted(int number, int k, int p){
        return (((1 << k) - 1) & (number >> (p - 1)));
    }

    public String hexToBin(String s) {
        return new BigInteger(s, 16).toString(2);
    }
    public void decode(byte[] msg){
        String firstByte = hexToBin(String.valueOf(msg[0]));
        String secondByte = hexToBin(String.valueOf(msg[1]));
        source = bitExtracted(Integer.parseInt(firstByte), 4, 0);
        destination = bitExtracted(Integer.parseInt(firstByte), 4, 4);
        packetType = bitExtracted(Integer.parseInt(secondByte), 2, 0);
        seqNr = msg[2];
    }
}
