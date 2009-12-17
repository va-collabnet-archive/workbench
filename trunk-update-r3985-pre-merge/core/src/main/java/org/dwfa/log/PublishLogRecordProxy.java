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
 * Created on Apr 24, 2005
 */
package org.dwfa.log;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.logging.LogRecord;

import net.jini.core.constraint.MethodConstraints;
import net.jini.core.constraint.RemoteMethodControl;
import net.jini.id.Uuid;
import net.jini.security.TrustVerifier;
import net.jini.security.proxytrust.ProxyTrustIterator;
import net.jini.security.proxytrust.SingletonProxyTrustIterator;
import net.jini.security.proxytrust.TrustEquivalence;

/**
 * @author kec
 * 
 */
public class PublishLogRecordProxy implements I_PublishLogRecord, Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private I_PublishLogRecord backend;
    private Uuid id;

    /**
     * @throws RemoteException
     * 
     */
    public PublishLogRecordProxy(I_PublishLogRecord backend) throws RemoteException {
        super();
        this.backend = backend;
        this.id = backend.getId();
        // System.out.println("new Log record proxy: " + this.id);
    }

    /**
     * Create a smart proxy, using an implementation that supports constraints
     * if the server proxy does.
     * 
     * @throws RemoteException
     */
    static PublishLogRecordProxy create(I_PublishLogRecord serverProxy) throws RemoteException {
        return (serverProxy instanceof RemoteMethodControl) ? new ConstrainableProxy(serverProxy)
                                                           : new PublishLogRecordProxy(serverProxy);
    }

    public boolean equals(Object o) {
        return getClass() == o.getClass() && getServerProxy().equals(((PublishLogRecordProxy) o).getServerProxy());
    }

    public int hashCode() {
        return getServerProxy().hashCode();
    }

    /** A constrainable implementation of the smart proxy. */
    private static final class ConstrainableProxy extends PublishLogRecordProxy implements RemoteMethodControl {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        ConstrainableProxy(I_PublishLogRecord serverProxy) throws RemoteException {
            super(serverProxy);
        }

        /** Implement RemoteMethodControl */

        public MethodConstraints getConstraints() {
            return ((RemoteMethodControl) getServerProxy()).getConstraints();
        }

        public RemoteMethodControl setConstraints(MethodConstraints mc) {
            try {
                return new ConstrainableProxy(
                    (I_PublishLogRecord) ((RemoteMethodControl) getServerProxy()).setConstraints(mc));
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
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
        Verifier(I_PublishLogRecord serverProxy) {
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
    protected I_PublishLogRecord getServerProxy() {
        return backend;
    }

    /**
     * @see org.dwfa.log.I_PublishLogRecord#publish(java.util.logging.LogRecord)
     */
    public void publish(LogRecord record) throws RemoteException {
        if (backend == this) {
            throw new RemoteException("backend == this");
        }
        // System.out.println("publish: " + backend + " " + record.getMessage()
        // + " " +record.getSequenceNumber());
        backend.publish(record);

    }

    /**
     * @see org.dwfa.log.I_PublishLogRecord#getId()
     */
    public Uuid getId() throws RemoteException {
        return this.id;
    }

}
