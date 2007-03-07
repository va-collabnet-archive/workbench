/*
 * Created on Apr 18, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
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
 * See page 77 of JavaSpaces Principles, Patterns, and Practice
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
