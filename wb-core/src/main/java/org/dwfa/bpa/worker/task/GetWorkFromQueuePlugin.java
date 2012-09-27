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
package org.dwfa.bpa.worker.task;

import java.util.logging.Level;

import net.jini.core.transaction.Transaction;

import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_QueueProcesses;
import org.dwfa.bpa.process.I_SelectProcesses;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.NoMatchingEntryException;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.worker.Worker;

public class GetWorkFromQueuePlugin implements I_GetWorkFromQueue, Runnable {

    private I_Work worker;
    private Thread workerThread;
    private I_QueueProcesses queue;
    private boolean sleeping;
    private long sleepTime = 1000 * 60 * 1;
    private String desc;
    private I_SelectProcesses selector;

    public GetWorkFromQueuePlugin(I_Work worker, I_QueueProcesses queue, I_SelectProcesses selector) {
        super();
        this.worker = worker;
        this.queue = queue;
        this.selector = selector;
    }

    public void queueContentsChanged() {
        if (this.sleeping) {
            this.workerThread.interrupt();
        }
    }

    /**
     * @see org.dwfa.bpa.worker.task.I_GetWorkFromQueue#start(org.dwfa.bpa.process.I_QueueProcesses)
     */
    public void start(I_QueueProcesses queue) {
        this.queue = queue;
        this.desc = "Worker " + worker.getWorkerDesc() + " " + this.getClass().getName();
        this.workerThread = new Thread(this, desc);
        this.workerThread.start();
    }

    public void sleep() {
        this.sleeping = true;
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {

        }
        this.sleeping = false;
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        Transaction t;
        while (true) {
            try {
                while (true) {
                    try {
                        t = worker.getActiveTransaction();
                        I_EncodeBusinessProcess process = this.queue.take(selector, t);
                        if (worker.getLogger().isLoggable(Level.INFO)) {
                            worker.getLogger().info(
                                desc + " TAKE: " + process.getName() + " (" + process.getProcessID() + ") " + ": "
                                    + process.getCurrentTaskId() + " "
                                    + process.getTask(process.getCurrentTaskId()).getName() + " deadline: "
                                    + Worker.dateFormat.format(process.getDeadline()));
                        }

                        worker.execute(process);
                        worker.commitTransactionIfActive();
                    } catch (TaskFailedException ex) {
                        worker.discardActiveTransaction();
                        worker.getLogger().log(Level.WARNING,
                            "Worker: " + desc + " (" + worker.getId() + ") " + ex.getMessage(), ex);
                    }
                }

            } catch (NoMatchingEntryException ex) {
                try {
                    worker.abortActiveTransaction();
                } catch (Exception e) {
                    worker.getLogger().log(Level.SEVERE,
                        "Worker: " + desc + " (" + worker.getId() + ") " + e.getMessage(), e);
                }
                if (worker.getLogger().isLoggable(Level.FINE)) {
                    worker.getLogger().fine(desc + " (" + worker.getId() + ") started sleep.");
                }
                this.sleep();
                if (worker.getLogger().isLoggable(Level.FINE)) {
                    worker.getLogger().fine(desc + " (" + worker.getId() + ") awake.");
                }
            } catch (Throwable ex) {
                worker.discardActiveTransaction();
                worker.getLogger().log(Level.SEVERE, desc, ex);
            }
        }

    }

}
