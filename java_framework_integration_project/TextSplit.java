import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;

public class TextSplit {

    public byte[] textToBytes(String msg){
        byte[] byteArray = msg.getBytes(StandardCharsets.UTF_8);
        return byteArray;
    }

    public List[][] splitTextBytes(byte[] msg, int size){
        ArrayList[][] listOfList = new ArrayList[(msg.length/size) + 1][];
        for(int j = 0; j < msg.length; j += msg.length) {
            ArrayList pkt[] = new ArrayList[size];
            if (msg.length - size >= size) {
                for (int i = 0; i < size; i++) {
                    pkt[i].add(msg[i]);
                }
            } else {
                for (int i = 0; i < abs(msg.length - size); i++){
                    pkt[i].add(msg[i]);
                }
            }
            listOfList = new ArrayList[][]{pkt};
        }
        return listOfList;
    }

    public void arrayOfArrayBackToText(ArrayList[][] msg){

    }
}
