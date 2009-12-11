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
package org.dwfa.mojo.refset;

import java.util.LinkedList;
import java.util.logging.Logger;

public class ProcessQueue {
    private Logger logger = Logger.getLogger(this.getClass().getName());
    private final int nThreads;
    private final PoolWorker[] threads;
    private final LinkedList<Runnable> queue;

    public ProcessQueue(int nThreadsToUse) {
        this.nThreads = nThreadsToUse;
        queue = new LinkedList<Runnable>();
        threads = new PoolWorker[nThreads];

        for (int i = 0; i < nThreads; i++) {
            threads[i] = new PoolWorker();
            threads[i].start();
        }
    }

    public void execute(Runnable r) {
        synchronized (queue) {
            queue.addLast(r);
            queue.notify();
        }
    }

    public boolean isEmpty() {
        boolean isEmpty = true;

        for (PoolWorker poolWorker : threads) {
            isEmpty = isEmpty && poolWorker.isEmpty();
        }

        return isEmpty;
    }

    private class PoolWorker extends Thread {
        public void run() {
            Runnable r;

            while (true) {
                synchronized (queue) {
                    while (queue.isEmpty()) {
                        try {
                            queue.wait();
                        } catch (InterruptedException ignored) {
                        }
                    }

                    r = (Runnable) queue.removeFirst();
                }

                try {
                    r.run();
                } catch (RuntimeException e) {
                    logger.severe("Error: Processing concept.");
                    throw e;
                }
            }
        }

        boolean isEmpty() {
            return queue.isEmpty();
        }
    }
}
