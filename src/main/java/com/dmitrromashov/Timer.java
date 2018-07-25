package com.dmitrromashov;

import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class Timer {
    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    public void start(Runnable runnable, long initialDelay, long delay, TimeUnit timeUnit){
        scheduledExecutorService.scheduleWithFixedDelay(runnable, initialDelay, delay, timeUnit);
    }

    @PreDestroy
    private void destroy(){
        System.out.println("Timer destroy");
        scheduledExecutorService.shutdown();
        try {
            scheduledExecutorService.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


}
