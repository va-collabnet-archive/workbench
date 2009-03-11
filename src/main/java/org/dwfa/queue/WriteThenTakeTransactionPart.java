/*
 * Created on Apr 21, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.queue;

import java.io.File;
import java.util.Date;
import java.util.SortedSet;

import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.server.TransactionManager;

import org.dwfa.bpa.process.I_DescribeObject;
import org.dwfa.jini.I_TransactionPart;



public class WriteThenTakeTransactionPart<T extends I_DescribeObject> implements I_TransactionPart {

    private T processDesc;
    private SortedSet<T> processesInfoSortedSet;
    private File processFile;
    private Transaction takeTran;
    private ObjectServerCore server;
    

    /**
     * @param processDesc
     * @param processesInfoSortedSet
     * @param uncommittedTakes
     * @param processFile
     */
    public WriteThenTakeTransactionPart(T processDesc,
            SortedSet<T> processesInfoSortedSet,
            File processFile, Transaction takeTran, ObjectServerCore server) {
        super();
        this.processDesc = processDesc;
        this.processesInfoSortedSet = processesInfoSortedSet;
        this.processFile = processFile;
        this.takeTran = takeTran;
        this.server = server;
    }


    /**
     * @see org.dwfa.jini.I_TransactionPart#commit(net.jini.core.transaction.server.TransactionManager, long, java.util.Date)
     */
    public void commit(TransactionManager mgr, long id, Date commitDate) {
        this.server.finishWrite(processFile);
        this.processesInfoSortedSet.add(processDesc);
    }

    /**
     * @see org.dwfa.jini.I_TransactionPart#abort(net.jini.core.transaction.server.TransactionManager, long)
     */
    public void abort(TransactionManager mgr, long id) {
        processFile.delete();
        try {
            this.takeTran.abort();
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }

}