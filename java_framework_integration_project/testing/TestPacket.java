package testing;

import client.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import protocol.Packet;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestPacket {
    Packet pck;

    @BeforeEach
    void setup() {
        pck = new Packet();
    }

    @Test
    void testBitExtracted() {
        int numToGet;
        int returnValue;

        numToGet = 0xff;
        returnValue = pck.bitExtracted(numToGet, 8, 0);
        assertEquals(numToGet, returnValue);

        numToGet = 0x01;
        returnValue = pck.bitExtracted(numToGet, 1, 0);
        assertEquals(numToGet, returnValue);
    }

    @Test
    void testDecodeData() {
        // TODO update the test when data is being properly decoded
        byte[] msg = new byte[32];
        MessageType msgType = MessageType.DATA;
        msg[0] = (byte) 0xf1;
        msg[1] = (byte) 0x00;
        msg[2] = (byte) 0x01;

        pck.decode(msg, msgType);

        assertEquals(0x0f, pck.getSource());
        assertEquals(0x01, pck.getDestination());
        assertEquals(0x00, pck.getPacketType());
        assertEquals(0x01, pck.getSeqNr());
    }
}
