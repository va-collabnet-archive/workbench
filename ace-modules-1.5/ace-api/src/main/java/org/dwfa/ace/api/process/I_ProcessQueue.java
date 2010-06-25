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
package org.dwfa.ace.api.process;

/**
 * Interface for a "process queue" which can concurrently execute Runnable
 * objects for the caller using an underlying pool of threads.
 * 
 */
public interface I_ProcessQueue {

    /**
     * Executes the provided Runnable concurrently. Depending upon the
     * number of pooled threads and active jobs this Runnable may be executed
     * immediately or may be queued waiting for an available worker thread.
     * 
     * @param r object to execute
     */
    void execute(Runnable r);

    /**
     * Indicates if there are any queued jobs. Note that no queued jobs does not
     * mean that there are no jobs still being executed, just that the queue
     * of jobs waiting to execute is empty.
     * 
     * @return true if the queue of Runnables waiting to be executed is empty,
     *         false otherwise.
     */
    boolean isEmpty();

    /**
     * This method allows the calling thread to block and await completion of
     * all
     * active and waiting Runnable objects the process queue is managing. Once
     * this
     * method is called the queue will no longer accept new jobs until all
     * running and
     * queued jobs are complete and this method has returned.
     */
    void awaitCompletion();
}
