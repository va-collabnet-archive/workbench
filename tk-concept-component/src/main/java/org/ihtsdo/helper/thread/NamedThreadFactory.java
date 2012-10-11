/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.helper.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A factory for creating NamedThread objects.
 */
public class NamedThreadFactory implements ThreadFactory {

    AtomicInteger factoryCount = new AtomicInteger();
    ThreadGroup threadGroup;
    String threadNamePrefix;
    int threadPriority;

    /**
     * Instantiates a new named thread factory.
     *
     * @param threadGroup the thread group to use
     * @param threadNamePrefix the <code>String</code> describing the thread group
     */
    public NamedThreadFactory(ThreadGroup threadGroup,
            String threadNamePrefix) {
        this(threadGroup, threadNamePrefix, Thread.NORM_PRIORITY);
    }

    /**
     * Instantiates a new named thread factory. Can specify the priority of the threads.
     *
     * @param threadGroup the thread group to use
     * @param threadNamePrefix the <code>String</code> describing the thread group
     * @param threadPriority an <code>int<code> representing the thread's priority, higher numbers represent higher priority
     */
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

    /**
     * Created a new thread based on this factory's naming and priority.
     * @param r the object whose <code>run</code> method is invoked when this thread is started.
     * @return the generated thread
     * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
     */
    @Override
    public Thread newThread(Runnable r) {
        String threadName = threadNamePrefix + " "
                + factoryCount.incrementAndGet();
        //AceLog.getAppLog().info("Creating thread: " + threadName);
        Thread t = new Thread(threadGroup, r, threadName);
        t.setPriority(threadPriority);
        return t;
    }
}