import java.nio.charset.StandardCharsets;

public class TextSplit {

    public byte[] bytes;

    public void textToBytes(String msg){
        bytes = msg.getBytes(StandardCharsets.UTF_8);
    }

    public void splitTextBytes(String msg, int size){

    }

    public void arrayOfArrayBackToText(){

    }
}
