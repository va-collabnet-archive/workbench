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

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Collection;

import net.jini.core.constraint.MethodConstraints;
import net.jini.core.constraint.RemoteMethodControl;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.security.TrustVerifier;
import net.jini.security.proxytrust.ProxyTrustIterator;
import net.jini.security.proxytrust.SingletonProxyTrustIterator;
import net.jini.security.proxytrust.TrustEquivalence;

import org.dwfa.bpa.process.EntryID;
import org.dwfa.bpa.process.I_DescribeBusinessProcess;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_QueueProcesses;
import org.dwfa.bpa.process.I_SelectProcesses;
import org.dwfa.bpa.process.NoMatchingEntryException;
import org.dwfa.bpa.process.ProcessID;

/**
 * @author kec
 * 
 */
public class QueueProxy implements I_QueueProcesses, Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    I_QueueProcesses backend;

    /**
     * Create a smart proxy, using an implementation that supports constraints
     * if the server proxy does.
     */
    static QueueProxy create(I_QueueProcesses serverProxy) {
        return (serverProxy instanceof RemoteMethodControl) ? new ConstrainableProxy(serverProxy) : new QueueProxy(
            serverProxy);
    }

    public boolean equals(Object o) {
        return getClass() == o.getClass() && getServerProxy().equals(((QueueProxy) o).getServerProxy());
    }

    public int hashCode() {
        return getServerProxy().hashCode();
    }

    /** A constrainable implementation of the smart proxy. */
    private static final class ConstrainableProxy extends QueueProxy implements RemoteMethodControl {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        ConstrainableProxy(I_QueueProcesses serverProxy) {
            super(serverProxy);
        }

        /** Implement RemoteMethodControl */

        public MethodConstraints getConstraints() {
            return ((RemoteMethodControl) getServerProxy()).getConstraints();
        }

        public RemoteMethodControl setConstraints(MethodConstraints mc) {
            return new ConstrainableProxy(
                (I_QueueProcesses) ((RemoteMethodControl) getServerProxy()).setConstraints(mc));
        }

        /*
         * Provide access to the underlying server proxy to permit the
         * ProxyTrustVerifier class to verify the proxy.
         */
        @SuppressWarnings("unused")
        private ProxyTrustIterator getProxyTrustIterator() {
            return new SingletonProxyTrustIterator(getServerProxy());
        }
    }

    /** A trust verifier for secure smart proxies. */
    final static class Verifier implements TrustVerifier, Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        private final RemoteMethodControl serverProxy;

        /**
         * Create the verifier, throwing UnsupportedOperationException if the
         * server proxy does not implement both RemoteMethodControl and
         * TrustEquivalence.
         */
        Verifier(I_QueueProcesses serverProxy) {
            if (serverProxy instanceof RemoteMethodControl && serverProxy instanceof TrustEquivalence) {
                this.serverProxy = (RemoteMethodControl) serverProxy;
            } else {
                throw new UnsupportedOperationException();
            }
        }

        /** Implement TrustVerifier */
        public boolean isTrustedObject(Object obj, TrustVerifier.Context ctx) throws RemoteException {

            if (obj == null || ctx == null) {
                throw new NullPointerException();
            } else if (!(obj instanceof ConstrainableProxy)) {
                return false;
            }
            RemoteMethodControl otherServerProxy = (RemoteMethodControl) ((ConstrainableProxy) obj).getServerProxy();
            MethodConstraints mc = otherServerProxy.getConstraints();
            TrustEquivalence trusted = (TrustEquivalence) serverProxy.setConstraints(mc);
            return trusted.checkTrustEquivalence(otherServerProxy);
        }
    }

    /**
     * @return Returns the serverProxy.
     */
    protected I_QueueProcesses getServerProxy() {
        return backend;
    }

    /**
     * 
     */
    public QueueProxy(I_QueueProcesses backend) {
        super();
        this.backend = backend;
    }

    /**
     * @param selector
     * @return
     * @throws RemoteException
     * @throws IOException
     */
    public Collection<I_DescribeBusinessProcess> getProcessMetaData(I_SelectProcesses selector) throws RemoteException,
            IOException {
        return backend.getProcessMetaData(selector);
    }

    /**
     * @param processId
     * @param t
     * @return
     * @throws RemoteException
     * @throws NoMatchingEntryException
     */
    public I_EncodeBusinessProcess read(EntryID entryID, Transaction t) throws RemoteException, IOException,
            ClassNotFoundException, NoMatchingEntryException {
        return backend.read(entryID, t);
    }

    /**
     * @param selector
     * @param t
     * @return
     * @throws RemoteException
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws TransactionException
     * @throws NoMatchingEntryException
     */
    public I_EncodeBusinessProcess take(I_SelectProcesses selector, Transaction t) throws RemoteException, IOException,
            ClassNotFoundException, TransactionException, NoMatchingEntryException {
        return backend.take(selector, t);
    }

    /**
     * @param processId
     * @param t
     * @return
     * @throws RemoteException
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws TransactionException
     * @throws NoMatchingEntryException
     */
    public I_EncodeBusinessProcess take(EntryID entryID, Transaction t) throws RemoteException, IOException,
            ClassNotFoundException, TransactionException, NoMatchingEntryException {
        return backend.take(entryID, t);
    }

    /**
     * @param process
     * @param t
     * @throws RemoteException
     * @throws IOException
     * @throws TransactionException
     */
    public EntryID write(I_EncodeBusinessProcess process, Transaction t) throws RemoteException, IOException,
            TransactionException {
        return backend.write(process, t);
    }

    public void write(I_EncodeBusinessProcess process, EntryID entryID, Transaction t) throws RemoteException,
            IOException, TransactionException {
        backend.write(process, entryID, t);
    }

    /**
     * @param process
     * @param writeTran
     * @param takeTran
     * @throws RemoteException
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws TransactionException
     */
    public EntryID writeThenTake(I_EncodeBusinessProcess process, Transaction writeTran, Transaction takeTran)
            throws RemoteException, IOException, ClassNotFoundException, TransactionException {
        return backend.writeThenTake(process, writeTran, takeTran);
    }

    public String getNodeInboxAddress() throws RemoteException {
        return backend.getNodeInboxAddress();
    }

    public I_EncodeBusinessProcess take(ProcessID processID, Transaction t) throws RemoteException, IOException,
            ClassNotFoundException, TransactionException, NoMatchingEntryException {
        return backend.take(processID, t);
    }

    public void hide(EntryID entryID, Transaction t) throws RemoteException, IOException, ClassNotFoundException,
            TransactionException, NoMatchingEntryException {
        backend.hide(entryID, t);

    }
}
