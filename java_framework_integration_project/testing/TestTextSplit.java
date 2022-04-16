package testing;

import protocol.TextSplit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;


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
    }
}
