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
 * Created on Apr 20, 2005
 */
package org.dwfa.queue;

import org.dwfa.bpa.BusinessProcessInfo;
import org.dwfa.bpa.process.EntryID;
import org.dwfa.bpa.process.I_DescribeBusinessProcess;
import org.dwfa.bpa.process.I_DescribeObject;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_QueueProcesses;
import org.dwfa.bpa.process.I_SelectProcesses;
import org.dwfa.bpa.process.NoMatchingEntryException;
import org.dwfa.bpa.process.ProcessID;
import org.ihtsdo.ttk.queue.QueueAddress;
import org.ihtsdo.ttk.queue.QueuePreferences;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Logger;
import javax.transaction.Transaction;

/**
 * @author kec
 * 
 */
public class QueueServer extends ObjectServerCore<I_DescribeBusinessProcess>
        implements I_QueueProcesses, Comparable<ObjectServerCore<I_DescribeObject>> {

    private static ConcurrentSkipListSet<QueuePreferences> startedQueues = new ConcurrentSkipListSet<>();

    public static boolean started(QueuePreferences q) throws MalformedURLException {
        return startedQueues.contains(q);
    }

    protected static final Logger logger = Logger.getLogger(QueueServer.class.getName());

    /**
     * 
     */
    public QueueServer(QueuePreferences qp) throws Exception {
        super(qp);
        startedQueues.add(qp);
    }


    @Override
    public I_EncodeBusinessProcess take(EntryID entryID, Transaction t) throws IOException,
            ClassNotFoundException, NoMatchingEntryException {
        return (I_EncodeBusinessProcess) super.take(entryID, t);
    }
    
    @Override
    public void delete(EntryID entryID, Transaction t) throws NoMatchingEntryException {
        super.delete(entryID, t);
    }
    
    @Override
    public I_EncodeBusinessProcess take(ProcessID processID, Transaction t) throws IOException,
            ClassNotFoundException, NoMatchingEntryException {
        return (I_EncodeBusinessProcess) super.take(processID.getUuid(), t);
    }

    /**
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws NoMatchingEntryException
     * @see org.dwfa.bpa.process.I_QueueProcesses#read(net.jini.id.Uuid,
     *      net.jini.core.transaction.Transaction)
     */
    @Override
    public I_EncodeBusinessProcess read(EntryID entryID, Transaction t) throws IOException, ClassNotFoundException,
            NoMatchingEntryException {
        return (I_EncodeBusinessProcess) super.read(entryID, t);
    }

    /**
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws NoMatchingEntryException
     * @see org.dwfa.bpa.process.I_QueueProcesses#read(net.jini.id.Uuid,
     *      net.jini.core.transaction.Transaction)
     */
    public I_EncodeBusinessProcess read(ProcessID processID, Transaction t) throws IOException, ClassNotFoundException,
            NoMatchingEntryException {
        return (I_EncodeBusinessProcess) super.read(processID.getUuid(), t);
    }

    /**
     * @throws IOException 
     * @see org.dwfa.bpa.process.I_QueueProcesses#getProcessMetaData(org.dwfa.bpa.process.I_SelectProcesses)
     */
    @Override
    public Collection<I_DescribeBusinessProcess> getProcessMetaData(I_SelectProcesses selector) throws IOException {
        return getMetaData(selector);
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        
        for (ActionListener l: commitListeners) {
            l.actionPerformed(event);
        }
        // Listening for changes. Need to wake the workers if a change happens.
        
//        SUPPORT FOR QUEUE WORKERS WAS REMOVED...
//        Iterator<I_GetWorkFromQueue> workerItr = this.workerList.iterator();
//        while (workerItr.hasNext()) {
//            I_GetWorkFromQueue worker = workerItr.next();
//            worker.queueContentsChanged();
//        }

    }

    /**
     * @return Returns the file suffix.
     */
    @Override
    public String getFileSuffix() {
        return ".bp";
    }

    /**
     * @return Returns the file suffix for a pending take.
     */
    @Override
    public String getFileSuffixTakePending() {
        return ".bp.take-pending";
    }

    /**
     * @return Returns the file suffix for a pending write.
     */
    @Override
    public String getFileSuffixWritePending() {
        return ".bp.write-pending";
    }

    @Override
    protected I_DescribeBusinessProcess getObjectDescription(Object obj, EntryID entryID) throws IOException {
        return new BusinessProcessInfo((I_DescribeBusinessProcess) obj, entryID);
    }

    @Override
    public void write(I_EncodeBusinessProcess p, EntryID eid, Transaction t) throws RemoteException, IOException {
        super.write(p, eid, t);

    }

    @Override
    public I_EncodeBusinessProcess take(I_SelectProcesses p, Transaction t) throws RemoteException, IOException,
            ClassNotFoundException, NoMatchingEntryException {
        return (I_EncodeBusinessProcess) super.take(p, t);
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    public EntryID write(I_EncodeBusinessProcess process, Transaction t) throws RemoteException, IOException {
        return super.write(process, t);
    }

    @Override
    public String getNodeInboxAddress() {
        for (Object obj: getInstanceProperties()) {
            if (obj instanceof QueueAddress) {
                return obj.toString();
            }
        }
        return null;
    }


    @Override
    public int compareTo(ObjectServerCore<I_DescribeObject> o) {
        return this.directory.compareTo(o.directory);
    }

}
