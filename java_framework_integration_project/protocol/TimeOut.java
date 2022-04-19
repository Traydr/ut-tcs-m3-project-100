package protocol;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TimeOut implements Runnable{
    @Override
    public void run() {
        LocalTime then = LocalTime.now().plusSeconds(15);
        LocalTime now = LocalTime.now();
        while (then.isAfter(now)) {
            now = LocalTime.now();
        }
    }
}