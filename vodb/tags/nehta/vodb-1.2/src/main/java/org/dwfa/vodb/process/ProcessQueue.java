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

import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import org.dwfa.ace.api.process.I_ProcessQueue;

public class ProcessQueue implements I_ProcessQueue {
    private Logger logger = Logger.getLogger(this.getClass().getName());
    private final int nThreads;
    private final PoolWorker[] threads;
    private final LinkedList<Runnable> queue;
	private boolean acceptNoMore;
	private CountDownLatch latch;
	private String name;

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
            threads[i] = new PoolWorker();
            threads[i].start();
        }
    }

    /* (non-Javadoc)
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

    /* (non-Javadoc)
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
			int runningThreads = 0;
			for (PoolWorker thread : threads) {
				if (thread.isRunning()) {
					runningThreads++;
				}
			}
			logger.info("Awaiting completion of process queue "
					+ name + ", " + queue.size()
					+ " queued items " + runningThreads + " running jobs");
			latch = new CountDownLatch(queue.size() + runningThreads);
		}
		try {
			latch.await();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		logger.info("Finished latch process queue "
				+ name);
		synchronized (queue) {
			latch = null;
			acceptNoMore = false;
		}
		logger.info("Finished awaiting completion of process queue "
				+ name);
    }
    
    private class PoolWorker extends Thread {
		private boolean running;

		public void run() {
            Runnable r;

            while (true) {
                synchronized (queue) {
                	if (latch != null) {
                		logger.info("Worker thread " + name + "_" + System.identityHashCode(this) + " about to count down latch" + latch.getCount());
                		latch.countDown();
                	}
                    while (queue.isEmpty()) {
                    	setRunning(false);
                    	try {
                            logger.info("Worker thread " + name + "_" + System.identityHashCode(this) + " blocked waiting on queue for work");
                            queue.wait();
                        } catch (InterruptedException ignored) {
                        }
                    }

                    setRunning(true);
                    r = (Runnable) queue.removeFirst();
                    logger.info("Worker thread " + name + "_" + System.identityHashCode(this) + " dequeued " + r + " for processing, " + queue.size() + " remaining");
                }

                try {
                    r.run();
                } catch (RuntimeException e) {
                    logger.severe("Error: Processing." + r);
                    throw e;
                }
            }
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
