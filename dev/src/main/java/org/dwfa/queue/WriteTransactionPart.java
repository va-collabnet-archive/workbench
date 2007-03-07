/*
 * Created on Apr 21, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.queue;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Date;
import java.util.SortedSet;

import net.jini.core.transaction.server.TransactionManager;

import org.dwfa.bpa.process.I_DescribeObject;
import org.dwfa.jini.I_TransactionPart;



public class WriteTransactionPart<T extends I_DescribeObject> implements I_TransactionPart {

    private T processDesc;
    private SortedSet<T> processesInfoSortedSet;
    private File processFile;
    private ActionListener listener;
    private ObjectServerCore server;
    

    /**
     * @param processDesc
     * @param processesInfoSortedSet
     * @param uncommittedTakes
     * @param processFile
     * @param listener
     */
    public WriteTransactionPart(T processDesc,
            SortedSet<T> processesInfoSortedSet,
            File processFile, ActionListener listener, ObjectServerCore server) {
        super();
        this.processDesc = processDesc;
        this.processesInfoSortedSet = processesInfoSortedSet;
        this.processFile = processFile;
        this.listener = listener;
        this.server = server;
    }


    /**
     * @see org.dwfa.jini.I_TransactionPart#commit(net.jini.core.transaction.server.TransactionManager, long, java.util.Date)
     */
    public void commit(TransactionManager mgr, long id, Date commitDate) {
        this.server.finishWrite(processFile);
        this.processesInfoSortedSet.add(processDesc);
        this.listener.actionPerformed(new ActionEvent(this, 0, "commit"));
    }

    /**
     * @see org.dwfa.jini.I_TransactionPart#abort(net.jini.core.transaction.server.TransactionManager, long)
     */
    public void abort(TransactionManager mgr, long id) {
        processFile.delete();
    }

}