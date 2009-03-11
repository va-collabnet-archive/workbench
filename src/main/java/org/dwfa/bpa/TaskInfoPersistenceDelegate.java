/*
 * Created on Jan 9, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
 */
package org.dwfa.bpa;

import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.Statement;
import java.util.Collection;

public class TaskInfoPersistenceDelegate extends
    DefaultPersistenceDelegate {

    public TaskInfoPersistenceDelegate() {
        super();
    }
    /**
     * @see java.beans.DefaultPersistenceDelegate#initialize(java.lang.Class, java.lang.Object, java.lang.Object, java.beans.Encoder)
     */
    @Override
    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
        super.initialize(type, oldInstance, newInstance, out);
        
        TaskInfo ti = (TaskInfo) oldInstance;
        
        Collection<Branch> branches = ti.getBranches();
        for (Branch b: branches) {
            out.writeStatement(new Statement(oldInstance, "addBranch", new Object[] {b}));
        }
        
        Collection<ExecutionRecord> records = ti.getExecutionRecords();
        for (ExecutionRecord r: records) {
            out.writeStatement(new Statement(oldInstance, "addExecutionRecord", new Object[] {r}));
        }
    }

}
