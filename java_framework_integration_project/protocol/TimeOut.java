package protocol;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class TimeOut implements Runnable{
    int timeout;
    int random;
    ArrayList<Integer> timeoutInfo;
    LocalTime then;
    LocalTime now;
    MyProtocol myProtocol;

    /**
     * Base constructor to be used internally
     * @param time The least amount of time to wait
     * @param rand A max random amount of extra time to wait
     * @param protocol MyProcotol
     */
    private TimeOut(int time, int rand, MyProtocol protocol) {
        this.timeout = time;
        this.random = rand;
        this.myProtocol = protocol;
        this.now = LocalTime.now();
        this.then = LocalTime.now();
    }

    /**
     * Constructor to time out for already made arraylists
     * @param time The least amount of time to wait
     * @param rand A max random amount of extra time to wait
     * @param protocol MyProcotol
     * @param type The type of waiting, plus extra information
     */
    public TimeOut(int time, int rand, MyProtocol protocol, ArrayList<Integer> type) {
        this(time, rand, protocol);
        this.timeoutInfo = type;
    }

    /**
     * Constructor for single integers
     * @param time The least amount of time to wait
     * @param rand A max random amount of extra time to wait
     * @param protocol MyProcotol
     * @param type The type of waiting
     */
    public TimeOut(int time, int rand, MyProtocol protocol, int type) {
        this(time, rand, protocol);
        ArrayList<Integer> info = new ArrayList<>();
        info.add(type);
        timeoutInfo = info;
    }

    /**
     * Start the timeout
     */
    public void startTimeout() {
        this.then = LocalTime.now().plusSeconds(timeout + new Random().nextInt(random));
        this.now = LocalTime.now();
    }

    /**
     * Checks if the timeout has elapsed yet
     * @return true if it hasn't elapsed, false otherwise
     */
    public boolean isOngoing() {
        this.now = LocalTime.now();
        return then.isAfter(now);
    }

    @Override
    public void run() {
        startTimeout();
        while (isOngoing()) {
            // Do nothing
        }
        myProtocol.timeoutEntry(timeoutInfo);
    }
}