package com.vance.gamelive.codec;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author vance
 * @date 2019/5/9
 */
public class LiveTaskManager {


    private static volatile LiveTaskManager instance;

    private ThreadPoolExecutor THREAD_POOL_EXECUTOR;


    private LiveTaskManager() {
        THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
    }

    public static LiveTaskManager getInstance() {
        if (instance == null) {
            synchronized (LiveTaskManager.class) {
                if (instance == null) {
                    instance = new LiveTaskManager();
                }
            }
        }
        return instance;
    }


    public void execute(Runnable runnable) {
        THREAD_POOL_EXECUTOR.execute(runnable);
    }

}
