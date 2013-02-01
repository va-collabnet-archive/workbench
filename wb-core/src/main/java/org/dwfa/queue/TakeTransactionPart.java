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

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.bpa.process.I_DescribeObject;

//~--- JDK imports ------------------------------------------------------------

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;

import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;

import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

/**
 * @author kec
 *
 */
public class TakeTransactionPart<T extends I_DescribeObject> extends QueueTransactionPart {
    private T                processDesc;
    private SortedSet<T>     processesInfoSortedSet;
    private Set<T>           uncommittedTakes;
    private File             originalProcessFile;
    private File             newProcessFile;
    private ActionListener   listener;
    private ObjectServerCore server;

    /**
     * @param objDesc
     * @param processesInfoSortedSet2
     * @param uncommittedTakes2
     * @param originalProcessFile
     */
    public TakeTransactionPart(T objDesc, SortedSet<T> processesInfoSortedSet, Set<T> uncommittedTakes,
                               File newProcessFile, File originalProcessFile, ActionListener listener,
                               ObjectServerCore server) {
        super(UUID.randomUUID().toString());
        this.processDesc            = objDesc;
        this.processesInfoSortedSet = processesInfoSortedSet;
        this.uncommittedTakes       = uncommittedTakes;
        this.originalProcessFile    = originalProcessFile;
        this.newProcessFile         = newProcessFile;
        this.listener               = listener;
        this.server                 = server;
    }

    @Override
    public void commit(Xid xid, boolean onePhase) throws XAException {
        originalProcessFile.delete();
        newProcessFile.delete();
        this.uncommittedTakes.remove(processDesc);
    }

    @Override
    public void rollback(Xid xid) throws XAException {
        this.processesInfoSortedSet.add(processDesc);
        this.uncommittedTakes.remove(processDesc);
        this.server.undoTake(newProcessFile);
        this.listener.actionPerformed(new ActionEvent(this, 0, "abort"));
    }
}
