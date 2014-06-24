/**
 * 
 */
package org.ihtsdo.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {

    AtomicInteger factoryCount = new AtomicInteger();
    ThreadGroup threadGroup;
    String threadNamePrefix;

    public NamedThreadFactory(ThreadGroup threadGroup,
            String threadNamePrefix) {
        super();
        this.threadGroup = threadGroup;
        this.threadNamePrefix = threadNamePrefix;
    }

    @Override
    public Thread newThread(Runnable r) {
        String threadName = threadNamePrefix + " " + 
                factoryCount.incrementAndGet();
        //AceLog.getAppLog().info("Creating thread: " + threadName);
        return new Thread(threadGroup, r, threadName);
    }
}