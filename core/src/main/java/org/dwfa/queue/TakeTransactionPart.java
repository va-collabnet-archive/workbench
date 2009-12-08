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
 * Created on Apr 21, 2005
 */
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

/**
 * @author kec
 * 
 */
public class TakeTransactionPart<T extends I_DescribeObject> implements I_TransactionPart {

    private T processDesc;
    private SortedSet<T> processesInfoSortedSet;
    private Set<T> uncommittedTakes;
    private File processFile;
    private ActionListener listener;
    private ObjectServerCore server;

    /**
     * @param objDesc
     * @param processesInfoSortedSet2
     * @param uncommittedTakes2
     * @param processFile
     */
    public TakeTransactionPart(T objDesc, SortedSet<T> processesInfoSortedSet, Set<T> uncommittedTakes,
            File processFile, ActionListener listener, ObjectServerCore server) {
        super();
        this.processDesc = objDesc;
        this.processesInfoSortedSet = processesInfoSortedSet;
        this.uncommittedTakes = uncommittedTakes;
        this.processFile = processFile;
        this.listener = listener;
        this.server = server;
    }

    /**
     * @see org.dwfa.jini.I_TransactionPart#commit(net.jini.core.transaction.server.TransactionManager,
     *      long, java.util.Date)
     */
    public void commit(TransactionManager mgr, long id, Date commitDate) {
        processFile.delete();
        this.uncommittedTakes.remove(processDesc);
    }

    /**
     * @see org.dwfa.jini.I_TransactionPart#abort(net.jini.core.transaction.server.TransactionManager,
     *      long)
     */
    public void abort(TransactionManager mgr, long id) {
        this.processesInfoSortedSet.add(processDesc);
        this.uncommittedTakes.remove(processDesc);
        this.server.undoTake(processFile);
        this.listener.actionPerformed(new ActionEvent(this, 0, "abort"));
    }

}
