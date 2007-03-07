package org.dwfa.clock;

import java.io.Serializable;
import java.rmi.RemoteException;

import net.jini.core.constraint.MethodConstraints;
import net.jini.core.constraint.RemoteMethodControl;
import net.jini.security.TrustVerifier;
import net.jini.security.proxytrust.ProxyTrustIterator;
import net.jini.security.proxytrust.SingletonProxyTrustIterator;
import net.jini.security.proxytrust.TrustEquivalence;

public class TimerProxy implements I_KeepIncrementalTime, Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    I_KeepIncrementalTime backend;

    /**
     * Create a smart proxy, using an implementation that supports constraints
     * if the server proxy does.
     */
    static TimerProxy create(I_KeepIncrementalTime serverProxy) {
        return (serverProxy instanceof RemoteMethodControl) ? new ConstrainableProxy(
                serverProxy)
                : new TimerProxy(serverProxy);
    }

    public boolean equals(Object o) {
        return getClass() == o.getClass()
                && getServerProxy().equals(((TimerProxy) o).getServerProxy());
    }

    public int hashCode() {
        return getServerProxy().hashCode();
    }

    /** A constrainable implementation of the smart proxy. */
    private static final class ConstrainableProxy extends TimerProxy implements
            RemoteMethodControl {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        ConstrainableProxy(I_KeepIncrementalTime serverProxy) {
            super(serverProxy);
        }

        /** Implement RemoteMethodControl */

        public MethodConstraints getConstraints() {
            return ((RemoteMethodControl) getServerProxy()).getConstraints();
        }

        public RemoteMethodControl setConstraints(MethodConstraints mc) {
            return new ConstrainableProxy(
                    (I_KeepIncrementalTime) ((RemoteMethodControl) getServerProxy())
                            .setConstraints(mc));
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
        Verifier(I_KeepIncrementalTime serverProxy) {
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
            RemoteMethodControl otherServerProxy = (RemoteMethodControl) ((ConstrainableProxy) obj)
                    .getServerProxy();
            MethodConstraints mc = otherServerProxy.getConstraints();
            TrustEquivalence trusted = (TrustEquivalence) serverProxy
                    .setConstraints(mc);
            return trusted.checkTrustEquivalence(otherServerProxy);
        }
    }

    /**
     * @return Returns the serverProxy.
     */
    protected I_KeepIncrementalTime getServerProxy() {
        return backend;
    }

    /**
     * 
     */
    public TimerProxy(I_KeepIncrementalTime backend) {
        super();
        this.backend = backend;
    }

	public long getTime() throws RemoteException {
		return backend.getTime();
	}

	public void increment() throws RemoteException {
		backend.increment();
	}

	public void reset() throws RemoteException {
		backend.reset();
	}

}
