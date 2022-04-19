package protocol;

import java.time.Duration;
import java.time.LocalDateTime;

public class TimeOut implements Runnable{
    @Override
    public void run() {
        LocalDateTime then = LocalDateTime.now().plusSeconds(15);
        while (!(LocalDateTime.now().equals(then))) {

        }
    }
}