package protocol;

import java.time.LocalTime;
import java.util.Random;

public class TimeOut implements Runnable{
    int timeout;
    int random;
    int timeoutType;
    LocalTime then;
    LocalTime now;
    MyProtocol myProtocol;

    /**
     * Constructor to time out
     * @param time The least amount of time to wait
     * @param rand A max random amount of extra time to wait
     * @param protocol MyProcotol
     * @param type The type of waiting
     */
    public TimeOut(int time, int rand, MyProtocol protocol, int type) {
        this.timeout = time;
        this.random = rand;
        this.myProtocol = protocol;
        this.timeoutType = type;
        this.then = LocalTime.now();
        this.then = LocalTime.now();
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
        myProtocol.timeoutEntry(timeoutType);
    }
}