package protocol;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Random;

public class TimeOut implements Runnable{
    int timeout;
    int random;
    LocalTime then;
    LocalTime now;

    public TimeOut(int time, int rand) {
        this.timeout = time;
        this.random = rand;
    }

    public void startTimeout() {
        then = LocalTime.now().plusSeconds(timeout + new Random().nextInt(random));
        now = LocalTime.now();
    }

    @Override
    public void run() {
        startTimeout();
        while (then.isAfter(now)) {
            now = LocalTime.now();
        }
    }
}