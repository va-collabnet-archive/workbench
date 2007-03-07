package org.dwfa.queue;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Date;
import java.util.Set;
import java.util.SortedSet;

import net.jini.core.transaction.server.TransactionManager;

import org.dwfa.bpa.process.I_DescribeObject;
import org.dwfa.jini.I_TransactionPart;

public class HideTransactionPart<T extends I_DescribeObject>  implements I_TransactionPart {

    private T processDesc;
    private SortedSet<T> processesInfoSortedSet;
    private Set<T> uncommittedTakes;
    private File processFile;
    private ActionListener listener;
    private ObjectServerCore server;
    

    /**
     * @param processDesc
     * @param processesInfoSortedSet2
     * @param uncommittedTakes2
     * @param processFile
     */
    public HideTransactionPart(T processDesc,
            SortedSet<T> processesInfoSortedSet, Set<T> uncommittedTakes,
            File processFile, ActionListener listener, ObjectServerCore server) {
        super();
        this.processDesc = processDesc;
        this.processesInfoSortedSet = processesInfoSortedSet;
        this.uncommittedTakes = uncommittedTakes;
        this.processFile = processFile;
        this.listener = listener;
        this.server = server;
    }


    /**
     * @see org.dwfa.jini.I_TransactionPart#commit(net.jini.core.transaction.server.TransactionManager, long, java.util.Date)
     */
    public void commit(TransactionManager mgr, long id, Date commitDate) {
        this.processesInfoSortedSet.add(processDesc);
        this.uncommittedTakes.remove(processDesc);
        this.server.undoTake(processFile);
        this.listener.actionPerformed(new ActionEvent(this, 0, "commit"));
    }

    /**
     * @see org.dwfa.jini.I_TransactionPart#abort(net.jini.core.transaction.server.TransactionManager, long)
     */
    public void abort(TransactionManager mgr, long id) {
        this.processesInfoSortedSet.add(processDesc);
        this.uncommittedTakes.remove(processDesc);
        this.server.undoTake(processFile);
        this.listener.actionPerformed(new ActionEvent(this, 0, "abort"));
    }

}

