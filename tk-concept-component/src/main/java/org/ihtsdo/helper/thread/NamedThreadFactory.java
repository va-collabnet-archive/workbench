/**
 * 
 */
package org.ihtsdo.helper.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {

    AtomicInteger factoryCount = new AtomicInteger();
    ThreadGroup threadGroup;
    String threadNamePrefix;
    int threadPriority;

    public NamedThreadFactory(ThreadGroup threadGroup,
            String threadNamePrefix) {
        this(threadGroup, threadNamePrefix, Thread.NORM_PRIORITY);
    }

    public NamedThreadFactory(ThreadGroup threadGroup,
            String threadNamePrefix, int threadPriority) {
        super();
        this.threadGroup = threadGroup;
        this.threadNamePrefix = threadNamePrefix;
        this.threadPriority = threadPriority;
        if (threadGroup.getMaxPriority() < threadPriority) {
            threadGroup.setMaxPriority(threadPriority);
        }
    }

    @Override
    public Thread newThread(Runnable r) {
        String threadName = threadNamePrefix + " " + 
                factoryCount.incrementAndGet();
        //AceLog.getAppLog().info("Creating thread: " + threadName);
        Thread t = new Thread(threadGroup, r, threadName);
        t.setPriority(threadPriority);
        return t;
    }
}