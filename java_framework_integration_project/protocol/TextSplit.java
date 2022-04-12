package protocol;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class TextSplit {

    public byte[] textToBytes(String msg){
        return msg.getBytes(StandardCharsets.UTF_8);
    }

    public ArrayList<ArrayList<Byte>> splitTextBytes(byte[] msg, int size){
        ArrayList<ArrayList<Byte>> listOfList = new ArrayList<>();
        for(int j = 0; j < msg.length; j += size) {
            ArrayList<Byte> pkt = new ArrayList<>();
            int k = j;
            if ( k + size <= msg.length) {
                for (int i = 0; i < size; i++) {
                    pkt.add(i, msg[k]);
                    k++;
                }
            } else {
                for (int i = 0; i < (msg.length - k); i++){
                    pkt.add(i, msg[k]);
                    k++;
                }
            }
            listOfList.add(pkt);
        }
        return listOfList;
    }

    public String arrayOfArrayBackToText(ArrayList<ArrayList<Byte>> msg){
        StringBuilder original = new StringBuilder();
        for (ArrayList<Byte> bytes : msg) {
            for (Byte aByte : bytes) {
                String s = new String(new byte[]{aByte}, StandardCharsets.UTF_8);
                original.append(s);
            }
        }
        return original.toString();
    }
}
