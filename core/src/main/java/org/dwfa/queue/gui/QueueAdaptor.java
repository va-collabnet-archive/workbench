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
/*
 * Created on Jan 21, 2006
 */
package org.dwfa.queue.gui;

import net.jini.core.lookup.ServiceID;

import org.dwfa.bpa.process.I_QueueProcesses;

public class QueueAdaptor implements Comparable<QueueAdaptor> {
    I_QueueProcesses queue;

    String queueName;

    ServiceID id;

    /**
     * @param queue
     * @param queueName
     * @param id
     */
    public QueueAdaptor(I_QueueProcesses queue, String queueName, ServiceID id) {
        super();
        this.queue = queue;
        this.queueName = queueName;
        this.id = id;
    }

    public String toString() {
        return this.queueName;
    }

    public int compareTo(QueueAdaptor q) {
        return queueName.compareTo(q.queueName);
    }
}
