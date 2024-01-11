package ru.sparural.kafka.consumer;

import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.*;

/**
 * @author Vorobyev Vyacheslav
 */
public class RequestGateway {
    private final ExecutorService executorService;

    public RequestGateway(String threadName, int startThreadPoolCount, int maxThreadPoolCount) {
        executorService = new ThreadPoolExecutor(startThreadPoolCount, maxThreadPoolCount,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                new CustomizableThreadFactory(threadName));
    }

    public RequestGateway(String threadName) {
        this(threadName, 0, Integer.MAX_VALUE);
    }

    public void submitTask(Runnable runnable) {
        executorService.submit(runnable);
    }
}