import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class TextSplit {

    public static byte[] textToBytes(String msg){
        byte[] byteArray = msg.getBytes(StandardCharsets.UTF_8);
        return byteArray;
    }

    public static ArrayList<ArrayList<String>> splitTextBytes(byte[] msg, int size){
        ArrayList<ArrayList<String>> listOfList = new ArrayList<>();
        for(int j = 0; j < msg.length; j += size) {
            ArrayList pkt = new ArrayList();
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

    public void arrayOfArrayBackToText(ArrayList<ArrayList<String>> msg){

    }
}
