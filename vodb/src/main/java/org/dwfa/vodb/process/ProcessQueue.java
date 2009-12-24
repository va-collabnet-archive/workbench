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

import org.dwfa.ace.api.process.I_ProcessQueue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProcessQueue implements I_ProcessQueue {
    private Logger logger = Logger.getLogger(this.getClass().getName());
    private final int nThreads;
    private final PoolWorker[] threads;
    private final LinkedList<Runnable> queue;
    private boolean acceptNoMore;
    private CountDownLatch latch;
    private String name;
    private boolean isComplete;
    private List<Exception> errorsList = Collections.synchronizedList(new ArrayList<Exception>());

    public ProcessQueue(int nThreadsToUse) {
        this(Integer.toHexString(System.identityHashCode(new String())), nThreadsToUse);
    }

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

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.process.I_ProcessQueue#execute(java.lang.Runnable)
     */
    public void execute(Runnable r) {
        synchronized (queue) {
            if (acceptNoMore) {
                throw new RuntimeException("Cannot add more while queue has been instructed to wait for completion");
            }
            queue.addLast(r);
            queue.notify();
        }
        logger.info("Added new task to the process queue " + r);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.process.I_ProcessQueue#isEmpty()
     */
    public boolean isEmpty() {
        boolean isEmpty = true;

        for (PoolWorker poolWorker : threads) {
            isEmpty = isEmpty && poolWorker.isEmpty();
        }

        return isEmpty;
    }

    public void awaitCompletion() {
        synchronized (queue) {
            acceptNoMore = true;
            int runningThreads = getRunningPoolWorkerCount();
            logger.info("Awaiting completion of process queue " + name + ", " + queue.size() + " queued items "
                + runningThreads + " running jobs");
            latch = new CountDownLatch(queue.size());
            queue.notifyAll();
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        logger.info("Finished latch process queue " + name);
        synchronized (queue) {
            latch = null;
            acceptNoMore = false;
            isComplete = true;
            queue.notifyAll();
            logger.info(getRunningPoolWorkerCount() + " remaining threads still alive on completion. Errors : " +
                    errorsList.size());
        }

        while(getRunningPoolWorkerCount() > 0) {
            logger.info("Waiting for " + getRunningPoolWorkerCount() + " threads to complete.");
            synchronized(queue) {
                try { queue.wait(1000); } catch (InterruptedException e) { /*do nothing*/ }
            }
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

    private class PoolWorker extends Thread {
        private boolean running;
        private final int id;

        public PoolWorker(final int id) {
            this.id = id;
        }

        public void run() {
            Runnable r;

            while (true) {
                synchronized (queue) {
                    if (isComplete) {
                        logger.info("work is complete. " + getWorkerName() + " returning.");
                        setRunning(false);
                        return;
                    }

                    while (queue.isEmpty()) {
                        if (isComplete) {
                            logger.info("Queue is empty but work is complete. " + getWorkerName() + " returning.");
                            setRunning(false);
                            return;
                        }

                        setRunning(false);
                        try {
                            logger.info(getWorkerName() + " blocked and waiting for work.");
                            queue.wait();
                        } catch (InterruptedException ignored) {
                        }
                    }

                    if (latch != null) {
                        logger.info(getWorkerName() + " about to count down latch from " + getLatchCount());
                        latch.countDown();
                    }

                    setRunning(true);
                    r = queue.removeFirst();
                    logger.info(getWorkerName() + " dequeued " + r +
                            " for processing. Latch count: " + getLatchCount() + " Queue size: " + queue.size());
                }

                try {
                    r.run();
                } catch (Exception e) {
                    logger.severe("Error: Processing." + r + " on " + getWorkerName() + ". " +
                            "Latch count : " + getLatchCount() + " Queue size : " + queue.size());
                    errorsList.add(e);
                }
            }
        }

        private String getWorkerName() {
            return "PoolWorker[" + id + "]";
        }

        private String getLatchCount() {
            return latch == null ?  "null" : String.valueOf(latch.getCount());
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
