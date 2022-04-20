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

    public TimeOut(int time, int rand, MyProtocol protocol, int type) {
        this.timeout = time;
        this.random = rand;
        this.myProtocol = protocol;
        this.timeoutType = type;
        this.then = LocalTime.now();
        this.then = LocalTime.now();
    }

    public void startTimeout() {
        this.then = LocalTime.now().plusSeconds(timeout + new Random().nextInt(random));
        this.now = LocalTime.now();
    }

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