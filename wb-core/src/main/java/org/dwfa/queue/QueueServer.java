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

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.rmi.server.ExportException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import net.jini.core.entry.Entry;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.export.Exporter;
import net.jini.export.ProxyAccessor;
import net.jini.lookup.entry.ServiceInfo;
import net.jini.security.TrustVerifier;
import net.jini.security.proxytrust.ServerProxyTrust;

import org.dwfa.bpa.BusinessProcessInfo;
import org.dwfa.bpa.process.EntryID;
import org.dwfa.bpa.process.I_DescribeBusinessProcess;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_QueueProcesses;
import org.dwfa.bpa.process.I_SelectProcesses;
import org.dwfa.bpa.process.NoMatchingEntryException;
import org.dwfa.bpa.process.ProcessID;
import org.dwfa.bpa.worker.task.I_GetWorkFromQueue;

import com.sun.jini.start.LifeCycle;

/**
 * @author kec
 * 
 */
public class QueueServer extends ObjectServerCore<I_DescribeBusinessProcess> implements I_QueueProcesses,
        ServerProxyTrust, ProxyAccessor {

    private static HashSet<String> startedQueues = new HashSet<String>();

    public static boolean started(File f) throws MalformedURLException {
        return startedQueues.contains(f.toURI().toURL().toExternalForm());
    }

    /** The server proxy, for use by getProxyVerifier */
    protected I_QueueProcesses serverProxy;
    protected static Logger logger = Logger.getLogger(QueueServer.class.getName());

    private List<I_GetWorkFromQueue> workerList = new ArrayList<I_GetWorkFromQueue>();

    /**
     * 
     */
    public QueueServer(String[] args, LifeCycle lc) throws Exception {
        super(args, lc);
        File file = new File(args[0]);
        startedQueues.add(file.toURI().toURL().toExternalForm());
        QueueWorkerSpec[] workerSpecs = (QueueWorkerSpec[]) this.config.getEntry(this.getClass().getName(),
            "workerSpecs", QueueWorkerSpec[].class);
        for (int i = 0; i < workerSpecs.length; i++) {
            I_GetWorkFromQueue worker = workerSpecs[i].create(this.config);
            worker.start(this);
            this.workerList.add(worker);
        }
    }

    /**
     * @return
     */
    protected Entry[] getFixedServiceEntries() {
        Entry[] moreEntries = new Entry[] { new ServiceInfo("Queue Service", "Informatics, Inc.", "Informatics, Inc.",
            VERSION_STRING, "Queue server", "no serial number") };
        return moreEntries;
    }

    /**
     * @param exporter
     * @throws ExportException
     */
    protected Object export(Exporter exporter) throws ExportException {
        serverProxy = (I_QueueProcesses) exporter.export(this);
        return QueueProxy.create(serverProxy);
    }

    /**
     * If the impl gets GC'ed, then the server will be unexported. Store the
     * instance here to prevent this.
     */
    @SuppressWarnings("unused")
    private static QueueServer serverImpl;

    public static void main(String[] args) throws Exception {

        serverImpl = new QueueServer(args, null);
        System.out.println("QueueServer is ready");
    }

    /**
     * Implement the ServerProxyTrust interface to provide a verifier for secure
     * smart proxies.
     */
    public TrustVerifier getProxyVerifier() {
        return new QueueProxy.Verifier(serverProxy);
    }

    /**
     * Returns a proxy object for this remote object.
     * 
     * @return our proxy
     */
    public Object getProxy() {
        return serverProxy;
    }

    public I_EncodeBusinessProcess take(EntryID entryID, Transaction t) throws TransactionException, IOException,
            ClassNotFoundException, NoMatchingEntryException {
        return (I_EncodeBusinessProcess) super.take(entryID, t);
    }

    public I_EncodeBusinessProcess take(ProcessID processID, Transaction t) throws TransactionException, IOException,
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
     * @see org.dwfa.bpa.process.I_QueueProcesses#getProcessMetaData(org.dwfa.bpa.process.I_SelectProcesses)
     */
    public Collection<I_DescribeBusinessProcess> getProcessMetaData(I_SelectProcesses selector) throws RemoteException {
        return getMetaData(selector);
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent arg0) {
        // Listening for changes. Need to wake the workers if a change happens.
        Iterator<I_GetWorkFromQueue> workerItr = this.workerList.iterator();
        while (workerItr.hasNext()) {
            I_GetWorkFromQueue worker = workerItr.next();
            worker.queueContentsChanged();
        }

    }

    /**
     * @return Returns the file suffix.
     */
    public String getFileSuffix() {
        return ".bp";
    }

    /**
     * @return Returns the file suffix for a pending take.
     */
    public String getFileSuffixTakePending() {
        return ".bp.take-pending";
    }

    /**
     * @return Returns the file suffix for a pending write.
     */
    public String getFileSuffixWritePending() {
        return ".bp.write-pending";
    }

    protected I_DescribeBusinessProcess getObjectDescription(Object obj, EntryID entryID) {
        return new BusinessProcessInfo((I_DescribeBusinessProcess) obj, entryID);
    }

    public void write(I_EncodeBusinessProcess p, EntryID eid, Transaction t) throws RemoteException, IOException,
            TransactionException {
        super.write(p, eid, t);

    }

    public I_EncodeBusinessProcess take(I_SelectProcesses p, Transaction t) throws RemoteException, IOException,
            ClassNotFoundException, TransactionException, NoMatchingEntryException {
        return (I_EncodeBusinessProcess) super.take(p, t);
    }

    public EntryID writeThenTake(I_EncodeBusinessProcess p, Transaction writeTran, Transaction takeTran)
            throws RemoteException, IOException, ClassNotFoundException, TransactionException {
        return super.writeThenTake(p, writeTran, takeTran);
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    public EntryID write(I_EncodeBusinessProcess process, Transaction t) throws RemoteException, IOException,
            TransactionException {
        return super.write(process, t);
    }

}
