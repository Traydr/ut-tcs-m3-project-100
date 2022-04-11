import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import static java.lang.Math.abs;

public class TextSplit {

    public static void main(String[] args) {
        String test = "i am home";
        byte[] bytes = textToBytes(test);
        System.out.println(Arrays.toString(bytes));
        ArrayList<ArrayList<String>> list = splitTextBytes(bytes, 4);
        System.out.println(list);

    }
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

//    public void arrayOfArrayBackToText(ArrayList[][] msg){
//
//    }
}
