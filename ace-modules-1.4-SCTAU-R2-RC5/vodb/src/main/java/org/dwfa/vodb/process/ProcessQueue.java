/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dwfa.vodb.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dwfa.ace.api.process.I_ProcessQueue;

/**
 * Thread pool implementation.
 *
 * Specify the number of threads in the pool then add work items.
 */
public class ProcessQueue implements I_ProcessQueue {
    private Logger logger = Logger.getLogger(this.getClass().getName());
    private final int nThreads;
    private final PoolWorker[] threads;
    private final LinkedList<Runnable> queue;
    private boolean acceptNoMore;
    private CountDownLatch latch;
    private String name;
    private List<Exception> errorsList = Collections.synchronizedList(new ArrayList<Exception>());
    private boolean failFast = true;
    private int maxQueuSize = 100;

    public ProcessQueue(int nThreadsToUse) {
        this(Integer.toHexString(System.identityHashCode(new String())), nThreadsToUse);
    }

    /**
     * Create the thread pool
     *
     * @param name String
     * @param nThreadsToUse int
     */
    public ProcessQueue(String name, int nThreadsToUse) {
        logger.info("Created new process queue '" + name + "' with " + nThreadsToUse + " threads");
        this.name = name;
        this.nThreads = nThreadsToUse;
        queue = new LinkedList<Runnable>();
        threads = new PoolWorker[nThreads];

        for (int i = 0; i < nThreads; i++) {
            threads[i] = new PoolWorker(i);
            threads[i].start();
        }
    }

    /**
     * @see org.dwfa.vodb.process.I_ProcessQueue#execute(java.lang.Runnable)
     */
    public void execute(Runnable r) {
        synchronized (queue) {
            if (acceptNoMore) {
                throw new RuntimeException("Cannot add more while queue has been instructed to wait for completion");
            }
            if (queue.size() > maxQueuSize) {
                try {
                    logger.finest("Waiting producer thread as queue size is over " + maxQueuSize);
                    queue.wait();
                } catch (InterruptedException egnored) {
                }
            }
            queue.addLast(r);
            queue.notify();
        }
        logger.info("Added new task to the process queue " + r);
    }

    /**
     * @see org.dwfa.vodb.process.I_ProcessQueue#isEmpty()
     */
    public boolean isEmpty() {
        boolean isEmpty = true;

        for (PoolWorker poolWorker : threads) {
            isEmpty = isEmpty && poolWorker.isEmpty();
        }

        return isEmpty;
    }

    /**
     * Pauses the calling thread until all items have been process.
     *
     * After this call completes you cannot use the this object to process any
     * further items.
     */
    public void awaitCompletion() {
        synchronized (queue) {
            acceptNoMore = true;
            int runningThreads = getRunningPoolWorkerCount();
            logger.info("Awaiting completion of process queue " + name + ", " + queue.size() + " queued items "
                + runningThreads + " running jobs");
            latch = new CountDownLatch(queue.size() + runningThreads);
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        logger.info("Finished latch process queue " + name);
        synchronized (queue) {
            queue.notifyAll();
        }
        failIfErrors();
        logger.info("Finished awaiting completion of process queue " + name);
    }

    private void failIfErrors() {
        if (!errorsList.isEmpty()) {
            logger.severe("The following errors was thrown when processes were executed:");
            for (Exception exception : errorsList) {
                logger.log(Level.SEVERE, exception.getMessage(), exception);
            }

            throw new RuntimeException("There were processing errors.");
        }
    }

    private int getRunningPoolWorkerCount() {
        int count = 0;
        for (PoolWorker thread : threads) {
            if (thread.isRunning()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Set to true to throw exceptions when processing and not store in the
     * errors report.
     *
     * @param failFast boolean.
     */
    public void setFailFast(boolean failFast) {
        this.failFast = failFast;
    }

    /**
     * Set the maximum queue size to process. Use to manage the heap space.
     *
     * Default 100
     *
     * @param maxQueuSize int
     */
    public final void setMaxQueuSize(int maxQueuSize) {
        this.maxQueuSize = maxQueuSize;
    }

    private class PoolWorker extends Thread {
        private boolean running;
        private final int id;

        public PoolWorker(final int id) {
            this.id = id;
        }

        public void run() {
            Runnable r = null;

            while (true) {
                synchronized (queue) {
                    if (latch != null) {
                        logger.finest("Worker thread " + name + "_" + System.identityHashCode(this)
                            + " about to count down latch " + latch.getCount());
                        latch.countDown();
                    }
                    while (queue.isEmpty()) {
                        setRunning(false);
                        if (acceptNoMore) {
                            return;
                        }
                        try {
                            logger.info("Worker thread " + name + "_" + System.identityHashCode(this)
                                + " blocked waiting on queue for work");
                            queue.wait();
                        } catch (InterruptedException ignored) {
                        }
                    }

                    setRunning(true);
                    if (!queue.isEmpty()) {
                        r = (Runnable) queue.removeFirst();
                        if (queue.size() == maxQueuSize) {
                            queue.notifyAll();
                        }
                        logger.finest("Worker thread " + name + "_" + System.identityHashCode(this) + " dequeued " + r
                            + " for processing, " + queue.size() + " remaining");
                    }
                }

                if (r != null) {
                    try {
                        r.run();
                    } catch (Throwable e) {
                        logger.severe("Error: Processing." + r + " on " + getWorkerName() + ". " + "Latch count : "
                            + getLatchCount() + " Queue size : " + queue.size());
                        errorsList.add(new Exception(e));

                        if (failFast) {
                            for (; latch.getCount() > 0;) {
                                latch.countDown();
                            }
                            throw new RuntimeException(e);
                        }

                    }
                }
            }
        }

        private String getWorkerName() {
            return "PoolWorker[" + id + "]";
        }

        private String getLatchCount() {
            return latch == null ? "null" : String.valueOf(latch.getCount());
        }

        boolean isEmpty() {
            return queue.isEmpty();
        }

        public void setRunning(boolean running) {
            synchronized (this) {
                this.running = running;
            }
        }

        public boolean isRunning() {
            synchronized (this) {
                return running;
            }
        }
    }
}
