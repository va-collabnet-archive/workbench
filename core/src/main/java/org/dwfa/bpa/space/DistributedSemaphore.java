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
 * Created on Apr 18, 2005
 */
package org.dwfa.bpa.space;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import net.jini.core.entry.Entry;
import net.jini.core.entry.UnusableEntryException;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.TransactionException;
import net.jini.space.JavaSpace;

/**
 * @author kec
 *         See page 77 of JavaSpaces Principles, Patterns, and Practice
 */
public class DistributedSemaphore {
    private JavaSpace space;
    private String resource;

    /**
     * @param space
     * @param resource
     */
    public DistributedSemaphore(JavaSpace space, String resource) {
        super();
        this.space = space;
        this.resource = resource;
    }

    public List<Lease> create(int num) throws TransactionException, IOException {
        List<Lease> leaseList = new ArrayList<Lease>();
        for (int i = 0; i < num; i++) {
            SemaphoreEntry semaphoreEntry = new SemaphoreEntry(resource);
            leaseList.add(space.write(semaphoreEntry, null, Lease.FOREVER));
        }
        return leaseList;
    }

    public void down() throws RemoteException, UnusableEntryException, TransactionException, InterruptedException {
        Entry template = new SemaphoreEntry(resource);
        space.take(template, null, Long.MAX_VALUE);
    }

    public Lease up() throws RemoteException, UnusableEntryException, TransactionException, InterruptedException {
        Entry semaphoreEntry = new SemaphoreEntry(resource);
        return space.write(semaphoreEntry, null, Long.MAX_VALUE);
    }
}
