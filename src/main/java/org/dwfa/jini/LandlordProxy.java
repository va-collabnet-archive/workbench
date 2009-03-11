/*
 * Created on Mar 7, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.jini;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Map;

import net.jini.core.constraint.MethodConstraints;
import net.jini.core.constraint.RemoteMethodControl;
import net.jini.core.lease.LeaseDeniedException;
import net.jini.core.lease.UnknownLeaseException;
import net.jini.id.Uuid;
import net.jini.security.TrustVerifier;
import net.jini.security.proxytrust.ProxyTrustIterator;
import net.jini.security.proxytrust.SingletonProxyTrustIterator;
import net.jini.security.proxytrust.TrustEquivalence;

import com.sun.jini.landlord.Landlord;

/**
 * @author kec
 *
 */
public class LandlordProxy implements Serializable, Landlord {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    Landlord backend;
    /**
     * Create a smart proxy, using an implementation that supports constraints
     * if the server proxy does.
     */
    static Landlord create(Landlord backend) {
        return (backend instanceof RemoteMethodControl) ? new ConstrainableProxy(
                backend)
                : new LandlordProxy(backend);
    }


    public boolean equals(Object o) {
        return getClass() == o.getClass()
                && backend.equals(((LandlordProxy) o).backend);
    }
    public int hashCode() {
	    return backend.hashCode();
    }


    /** A constrainable implementation of the smart proxy. */
    private static final class ConstrainableProxy extends LandlordProxy
            implements RemoteMethodControl {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        ConstrainableProxy(Landlord backend) {
            super(backend);
        }

        /** Implement RemoteMethodControl */

        public MethodConstraints getConstraints() {
            return ((RemoteMethodControl) backend).getConstraints();
        }

        public RemoteMethodControl setConstraints(MethodConstraints mc) {
            return new ConstrainableProxy(
                    (Landlord) ((RemoteMethodControl) backend)
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
        Verifier(Landlord serverProxy) {
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
	 * @param backend
	 */
	public LandlordProxy(Landlord backend) {
		super();
		this.backend = backend;
	}
	/**
	 * @see com.sun.jini.landlord.Landlord#renew(net.jini.id.Uuid, long)
	 */
	public long renew(Uuid cookie, long duration) throws LeaseDeniedException,
			UnknownLeaseException, RemoteException {
		return this.backend.renew(cookie, duration);
	}

	/**
	 * @see com.sun.jini.landlord.Landlord#cancel(net.jini.id.Uuid)
	 */
	public void cancel(Uuid cookie) throws UnknownLeaseException,
			RemoteException {
		this.backend.cancel(cookie);

	}

	/**
	 * @see com.sun.jini.landlord.Landlord#renewAll(net.jini.id.Uuid[], long[])
	 */
	public RenewResults renewAll(Uuid[] cookies, long[] durations)
			throws RemoteException {
		return this.backend.renewAll(cookies, durations);
	}

	/**
	 * @see com.sun.jini.landlord.Landlord#cancelAll(net.jini.id.Uuid[])
	 */
	@SuppressWarnings("unchecked")
	public Map<Uuid, UnknownLeaseException> cancelAll(Uuid[] cookies) throws RemoteException {
		return this.backend.cancelAll(cookies);
	}

}
