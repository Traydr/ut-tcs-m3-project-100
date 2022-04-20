package testing;

import protocol.TextSplit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;


public class TestTextSplit {
    TextSplit textsplit;

    @BeforeEach
    void setup() {
    }

    @Test
    void testTextToBytes() {
        assertEquals(0x61, TextSplit.textToBytes("a")[0]);
        assertEquals(0x41, TextSplit.textToBytes("A")[0]);

        assertEquals(0x68, TextSplit.textToBytes("hello")[0]);
        assertEquals(0x65, TextSplit.textToBytes("hello")[1]);
        assertEquals(0x6c, TextSplit.textToBytes("hello")[2]);
        assertEquals(0x6c, TextSplit.textToBytes("hello")[3]);
        assertEquals(0x6f, TextSplit.textToBytes("hello")[4]);

    }

    @Test
    void testSplitTextBytes() {
        ArrayList<Byte> testSplit = new ArrayList<>();
        testSplit.add((byte) 0x68);
        testSplit.add((byte) 0x69);
        assertEquals(testSplit, TextSplit.splitTextBytes(TextSplit.textToBytes("hi"), 2).get(0));

        ArrayList<Byte> testMaxSize = new ArrayList<>();
        testMaxSize.add((byte) 0x65);
        assertEquals(testMaxSize, TextSplit.splitTextBytes(TextSplit.textToBytes("abcdeabcdeabcdeabcdeabcdeabcde"), 29).get(1));
    }

    @Test
    void testArrayOfArrayBackToText() {
        ArrayList<ArrayList<Byte>> testArrayOfArray = new ArrayList<>();
        ArrayList<Byte> testArray = new ArrayList<>();
        testArray.add((byte) 0x74);
        testArray.add((byte) 0x65);
        testArray.add((byte) 0x73);
        testArray.add((byte) 0x74);
        testArrayOfArray.add(testArray);
        assertEquals("test", TextSplit.arrayOfArrayBackToText(testArrayOfArray, 4));
        assertNotEquals("test", TextSplit.arrayOfArrayBackToText(testArrayOfArray, 3));

        StringBuilder testString = new StringBuilder();
        byte[] testByteArray = new byte[40];
        for (int i = 0; i < 10; i++) {
            testByteArray[i * 4] = 0x74;
            testByteArray[i * 4 + 1] = 0x65;
            testByteArray[i * 4 + 2] = 0x73;
            testByteArray[i * 4 + 3] = 0x74;
            testString.append("test");
        }
        testArrayOfArray = TextSplit.splitTextBytes(testByteArray, 29);
        int testArrayLastSize = testArrayOfArray.get(testArrayOfArray.size() - 1).size();
        assertEquals(testString.toString(), TextSplit.arrayOfArrayBackToText(testArrayOfArray, testArrayLastSize));
        assertNotEquals(testString.toString(), TextSplit.arrayOfArrayBackToText(testArrayOfArray, testArrayLastSize - 1));
    }
}
