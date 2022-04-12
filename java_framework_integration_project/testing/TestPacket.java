package testing;

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

    @RepeatedTest(5)
    void testBitExtracted() {
        int numToGet;
        int returnValue;

        numToGet = 0xff;
        returnValue = pck.bitExtracted(numToGet, 8, 1);
        assertEquals(numToGet, returnValue);

        numToGet = 0x01;
        returnValue = pck.bitExtracted(numToGet, 1, 1);
        assertEquals(numToGet, returnValue);
    }
}
