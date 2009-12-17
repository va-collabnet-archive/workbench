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
import java.util.List;
import java.util.logging.Level;

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
public class LogManagerProxy implements I_ManageLogs, Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private I_ManageLogs backend;

    /**
     * Create a smart proxy, using an implementation that supports constraints
     * if the server proxy does.
     */
    static LogManagerProxy create(I_ManageLogs serverProxy) {
        return (serverProxy instanceof RemoteMethodControl) ? new ConstrainableProxy(serverProxy)
                                                           : new LogManagerProxy(serverProxy);
    }

    public boolean equals(Object o) {
        return getClass() == o.getClass() && getServerProxy().equals(((LogManagerProxy) o).getServerProxy());
    }

    public int hashCode() {
        return getServerProxy().hashCode();
    }

    /** A constrainable implementation of the smart proxy. */
    private static final class ConstrainableProxy extends LogManagerProxy implements RemoteMethodControl {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        ConstrainableProxy(I_ManageLogs serverProxy) {
            super(serverProxy);
        }

        /** Implement RemoteMethodControl */

        public MethodConstraints getConstraints() {
            return ((RemoteMethodControl) getServerProxy()).getConstraints();
        }

        public RemoteMethodControl setConstraints(MethodConstraints mc) {
            return new ConstrainableProxy((I_ManageLogs) ((RemoteMethodControl) getServerProxy()).setConstraints(mc));
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
        Verifier(I_ManageLogs serverProxy) {
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
    protected I_ManageLogs getServerProxy() {
        return backend;
    }

    /**
     * 
     */
    public LogManagerProxy(I_ManageLogs backend) {
        super();
        this.backend = backend;
    }

    /**
     * @see org.dwfa.log.I_ManageLogs#getLoggerNames()
     */
    public List<String> getLoggerNames() throws RemoteException {
        return this.backend.getLoggerNames();
    }

    /**
     * @see org.dwfa.log.I_ManageLogs#getLevel(java.lang.String)
     */
    public Object getLevel(String loggerName) throws RemoteException {
        return this.backend.getLevel(loggerName);
    }

    /**
     * @see org.dwfa.log.I_ManageLogs#isLoggable(java.lang.String,
     *      java.util.logging.Level)
     */
    public boolean isLoggable(String loggerName, Level level) throws RemoteException {
        return this.backend.isLoggable(loggerName, level);
    }

    /**
     * @return
     * @throws RemoteException
     * @see org.dwfa.log.I_ManageLogs#addRemoteHandler(java.lang.String,
     *      org.dwfa.log.I_PublishLogRecord)
     */
    public boolean addRemoteHandler(String loggerName, I_PublishLogRecord remoteHandler) throws RemoteException {
        return this.backend.addRemoteHandler(loggerName, remoteHandler);

    }

    /**
     * @throws RemoteException
     * @see org.dwfa.log.I_ManageLogs#removeRemoteHandler(java.lang.String,
     *      net.jini.id.Uuid)
     */
    public void removeRemoteHandler(String loggerName, Uuid id) throws RemoteException {
        this.backend.removeRemoteHandler(loggerName, id);

    }

}
