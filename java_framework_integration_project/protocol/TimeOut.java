package protocol;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Random;

public class TimeOut implements Runnable{
    int timeout;
    int random;

    public TimeOut(int time, int rand) {
        this.timeout = time;
        this.random = rand;
    }

    @Override
    public void run() {
        LocalTime then = LocalTime.now().plusSeconds(timeout + new Random().nextInt(random));
        LocalTime now = LocalTime.now();
        while (then.isAfter(now)) {
            now = LocalTime.now();
        }
    }
}