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
        int returnValue;

        returnValue = pck.bitExtracted(0xff, 8, 0);
        assertEquals(0xff, returnValue);

        returnValue = pck.bitExtracted(0xf6, 4, 4);
        assertEquals(0x0f, returnValue);

        returnValue = pck.bitExtracted(0x01, 1, 0);
        assertEquals(0x01, returnValue);
    }

    @Test
    void testDecodeData() {
        // TODO update the test when data is being properly decoded
        byte[] msg = new byte[32];
        MessageType msgType = MessageType.DATA;
        msg[0] = (byte) 0xf1;
        msg[1] = (byte) 0x01;
        msg[2] = (byte) 0x01;

        pck.decode(msg, msgType);

        assertEquals(0x0f, pck.getSource());
        assertEquals(0x01, pck.getDestination());
        assertEquals(0x00, pck.getPacketType());
        assertEquals(0x01, pck.getSeqNr());
        assertEquals(0x01, pck.getDataLen());
    }

    @Test
    void makePacketTest(){
        byte[] msg = new byte[32];
        byte[] packet;
        byte sourceDest = (byte) 0xf1;
        msg[0] = sourceDest;
        msg[1] = (byte) 0x00;
        msg[2] = (byte) 0x01;
        pck.setSource(0x0f); //15
        pck.setDestination(0x01); //1
        pck.setPacketType(0x00);
        pck.setSeqNr(0x01);

        MessageType msgType = MessageType.DATA;
        packet = pck.makePkt(msgType);

        assertEquals(sourceDest, packet[0]);
        assertEquals(0x00, packet[1]);
        assertEquals(0x01, packet[2]);
    }
}
