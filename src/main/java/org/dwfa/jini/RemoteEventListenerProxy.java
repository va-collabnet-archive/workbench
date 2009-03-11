/*
 * Created on Mar 7, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.jini;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import net.jini.core.constraint.MethodConstraints;
import net.jini.core.constraint.RemoteMethodControl;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import net.jini.security.TrustVerifier;
import net.jini.security.proxytrust.ProxyTrustIterator;
import net.jini.security.proxytrust.SingletonProxyTrustIterator;
import net.jini.security.proxytrust.TrustEquivalence;



/**
 * @author kec
 *
 */
public class RemoteEventListenerProxy implements Remote, RemoteEventListener, Serializable {
	/**
     * 
     */
    private static final long serialVersionUID = 1L;
    RemoteEventListener backend;
    /**
     * Create a smart proxy, using an implementation that supports constraints
     * if the server proxy does.
     */
    static RemoteEventListener create(RemoteEventListener backend) {
        return (backend instanceof RemoteMethodControl) ? new ConstrainableProxy(
                backend)
                : new RemoteEventListenerProxy(backend);
    }


    public boolean equals(Object o) {
        return getClass() == o.getClass()
                && backend.equals(((LandlordProxy) o).backend);
    }
    public int hashCode() {
	    return backend.hashCode();
    }


    /** A constrainable implementation of the smart proxy. */
    private static final class ConstrainableProxy extends RemoteEventListenerProxy
            implements RemoteMethodControl {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        ConstrainableProxy(RemoteEventListener backend) {
            super(backend);
        }

        /** Implement RemoteMethodControl */

        public MethodConstraints getConstraints() {
            return ((RemoteMethodControl) backend).getConstraints();
        }

        public RemoteMethodControl setConstraints(MethodConstraints mc) {
            return new ConstrainableProxy(
                    (RemoteEventListener) ((RemoteMethodControl) backend)
                            .setConstraints(mc));
        }

        /*
         * Provide access to the underlying server proxy to permit the
         * ProxyTrustVerifier class to verify the proxy.
         */
        @SuppressWarnings("unused")
		private ProxyTrustIterator getProxyTrustIterator() {
            return new SingletonProxyTrustIterator(backend);
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
        Verifier(RemoteEventListener serverProxy) {
            if (serverProxy instanceof RemoteMethodControl
                    && serverProxy instanceof TrustEquivalence) {
                this.serverProxy = (RemoteMethodControl) serverProxy;
            } else {
                throw new UnsupportedOperationException();
            }
        }

        /** Implement TrustVerifier */
        public boolean isTrustedObject(Object obj, TrustVerifier.Context ctx)
                throws RemoteException {
            if (obj == null || ctx == null) {
                throw new NullPointerException();
            } else if (!(obj instanceof ConstrainableProxy)) {
                return false;
            }
            RemoteMethodControl otherServerProxy = (RemoteMethodControl) ((ConstrainableProxy) obj).backend;
            MethodConstraints mc = otherServerProxy.getConstraints();
            TrustEquivalence trusted = (TrustEquivalence) serverProxy
                    .setConstraints(mc);
            return trusted.checkTrustEquivalence(otherServerProxy);
        }
    }


	/**
	 * 
	 */
	public RemoteEventListenerProxy(RemoteEventListener backend) {
		this.backend = backend;
	}

	/**
	 * @see net.jini.core.event.RemoteEventListener#notify(net.jini.core.event.RemoteEvent)
	 */
	public void notify(RemoteEvent theEvent) throws UnknownEventException,
			RemoteException {
		this.backend.notify(theEvent);

	}

}
