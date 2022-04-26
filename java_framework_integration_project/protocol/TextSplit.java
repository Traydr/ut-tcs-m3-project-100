package protocol;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class TextSplit {

    /**
     * Converts a String to a byte array
     *
     * @param msg String message
     * @return Byte array of UTF-8 encoding of msg
     */
    public static byte[] textToBytes(String msg) {
        return msg.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Converts a byte array to Arraylists of a certain size
     *
     * @param msg  byte array to convert
     * @param size size of the arraylists
     * @return Arraylist of Arraylist's of bytes
     */
    public static ArrayList<ArrayList<Byte>> splitTextBytes(byte[] msg, int size) {
        ArrayList<ArrayList<Byte>> listOfList = new ArrayList<>();
        for (int j = 0; j < msg.length; j += size) {
            ArrayList<Byte> pkt = new ArrayList<>();
            int k = j;
            if (k + size <= msg.length) {
                for (int i = 0; i < size; i++) {
                    pkt.add(msg[k]);
                    k++;
                }
            } else {
                for (int i = 0; i < msg.length - 29 * listOfList.size(); i++) {
                    pkt.add(msg[k]);
                    k++;
                }
            }
            listOfList.add(pkt);
        }
        return listOfList;
    }

    /**
     * Converts an arraylist of arraylist's of bytes into a string message
     *
     * @param msg            Arraylist of Arraylist's of bytes
     * @param lastMessageLen The length of the message in the last Arraylist
     * @return A string message
     */
    public static String arrayOfArrayBackToText(ArrayList<ArrayList<Byte>> msg, int lastMessageLen) {
        StringBuilder original = new StringBuilder();
        for (ArrayList<Byte> bytes : msg) {
            if (bytes != msg.get(msg.size() - 1)) {
                for (Byte aByte : bytes) {
                    String s = new String(new byte[]{aByte}, StandardCharsets.UTF_8);
                    original.append(s);
                }
            } else {
                for (int i = 0; i < lastMessageLen; i++) {
                    String s = new String(new byte[]{bytes.get(i)}, StandardCharsets.UTF_8);
                    original.append(s);
                }
            }

        }
        return original.toString();
    }
}
