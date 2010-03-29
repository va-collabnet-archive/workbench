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
package com.sun.jini.mahalo;

/*
 * This file class is based on sun's TxnManagerImpl, but is modified to operate
 * in a local only mode.
 * 
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

import java.rmi.RemoteException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationProvider;
import net.jini.core.discovery.LookupLocator;
import net.jini.core.entry.Entry;
import net.jini.core.lease.Lease;
import net.jini.core.lease.LeaseDeniedException;
import net.jini.core.lease.UnknownLeaseException;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.transaction.CannotAbortException;
import net.jini.core.transaction.CannotCommitException;
import net.jini.core.transaction.CannotJoinException;
import net.jini.core.transaction.TimeoutExpiredException;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.core.transaction.UnknownTransactionException;
import net.jini.core.transaction.server.CrashCountException;
import net.jini.core.transaction.server.ServerTransaction;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.core.transaction.server.TransactionParticipant;
import net.jini.id.Uuid;
import net.jini.id.UuidFactory;
import net.jini.security.TrustVerifier;
import net.jini.security.proxytrust.ServerProxyTrust;

import org.dwfa.jini.JiniManager;
import org.dwfa.jini.LookupJiniAndLocal;

import com.sun.jini.config.Config;
import com.sun.jini.landlord.FixedLeasePeriodPolicy;
import com.sun.jini.landlord.Landlord;
import com.sun.jini.landlord.LandlordUtil;
import com.sun.jini.landlord.LeaseFactory;
import com.sun.jini.landlord.LeasePeriodPolicy;
import com.sun.jini.landlord.LeasedResource;
import com.sun.jini.landlord.LocalLandlord;
import com.sun.jini.landlord.LeasePeriodPolicy.Result;
import com.sun.jini.logging.Levels;
import com.sun.jini.mahalo.log.CannotRecoverException;
import com.sun.jini.mahalo.log.LogException;
import com.sun.jini.mahalo.log.LogManager;
import com.sun.jini.mahalo.log.LogRecord;
import com.sun.jini.mahalo.log.LogRecovery;
import com.sun.jini.mahalo.log.MultiLogManager;
import com.sun.jini.start.LifeCycle;
import com.sun.jini.thread.InterruptedStatusThread;
import com.sun.jini.thread.ReadyState;
import com.sun.jini.thread.TaskManager;
import com.sun.jini.thread.WakeupManager;

/**
 * An implementation of the Jini(TM) Transaction Specification.
 * Modified to only operate "locally"...
 * 
 */
public class LocalTransactionManager implements TxnManager, LeaseExpirationMgr.Expirer, LogRecovery, TxnSettler,
        com.sun.jini.constants.TimeConstants, LocalLandlord, ServerProxyTrust {
    /** Logger for (successful) service startup message */
    static final Logger startupLogger = Logger.getLogger(TxnManager.MAHALO + ".startup");

    /** Logger for service re/initialization related messages */
    static final Logger initLogger = Logger.getLogger(TxnManager.MAHALO + ".init");

    /** Logger for service destruction related messages */
    static final Logger destroyLogger = Logger.getLogger(TxnManager.MAHALO + ".destroy");

    /** Logger for service operation messages */
    static final Logger operationsLogger = Logger.getLogger(TxnManager.MAHALO + ".operations");

    /**
     * Logger for transaction related messages (creation, destruction,
     * transition, etc.)
     */
    static final Logger transactionsLogger = Logger.getLogger(TxnManager.MAHALO + ".transactions");

    /** Logger for transaction participant related messages */
    static final Logger participantLogger = Logger.getLogger(TxnManager.MAHALO + ".participant");

    /** Logger for transaction persistence related messages */
    static final Logger persistenceLogger = Logger.getLogger(TxnManager.MAHALO + ".persistence");

    /**
     * @serial
     */
    private LogManager logmgr;

    /* Default tuning parameters for thread pool */
    /* Retrieve values from properties. */

    private transient int settlerthreads = 150;

    private transient long settlertimeout = 1000 * 15;

    private transient float settlerload = 1.0f;

    private transient int taskthreads = 50;

    private transient long tasktimeout = 1000 * 15;

    private transient float taskload = 3.0f;

    /* Its important here to schedule SettlerTasks on a */
    /* different TaskManager from what is given to */
    /* LocalTxnManagerTransaction objects. Tasks on a given */
    /* TaskManager which create Tasks cannot be on the */
    /* same TaskManager as their child Tasks. */

    private transient TaskManager settlerpool;

    /** wakeup manager for <code>SettlerTask</code> */
    private WakeupManager settlerWakeupMgr;

    private transient TaskManager taskpool;

    /** wakeup manager for <code>ParticipantTask</code> */
    private WakeupManager taskWakeupMgr;

    /*
     * Map of transaction ids are their associated, internal transaction
     * representations
     */
    private transient Map<Long, LocalTxnManagerTransaction> txns;

    private transient Vector<Long> unsettledtxns;

    private transient InterruptedStatusThread settleThread;

    /** The generator for our IDs. */
    private static transient SecureRandom idGen = new SecureRandom();

    /** The buffer for generating IDs. */
    private static transient final byte[] idGenBuf = new byte[8];

    /**
     * <code>LeaseExpirationMgr</code> used by our <code>LeasePolicy</code>.
     */
    private LeaseExpirationMgr expMgr;

    /**
     * @serial
     */
    private/* final */LeasePeriodPolicy txnLeasePeriodPolicy = null;

    /** <code>LandLordLeaseFactory</code> we use to create leases */
    private LeaseFactory leaseFactory = null;

    /**
     * The <code>Uuid</code> for this service. Used in the
     * <code>TxnMgrProxy</code> and <code>TxnMgrAdminProxy</code> to
     * implement reference equality. We also derive our <code>ServiceID</code>
     * from it.
     */
    private Uuid topUuid = null;

    /**
     * Cache of our inner proxy.
     */
    private TxnManager serverStub = null;

    /**
     * Cache of our <code>LifeCycle</code> object
     */
    @SuppressWarnings("unused")
    private LifeCycle lifeCycle = null;

    /**
     * Object used to prevent access to this service during the service's
     * initialization or shutdown processing.
     */
    private final ReadyState readyState = new ReadyState();

    /**
     * Constructs a non-activatable transaction manager.
     * 
     * @param args
     *            Service configuration options
     * 
     * @param lc
     *            <code>LifeCycle</code> reference used for callback
     */
    public LocalTransactionManager(String[] args, LifeCycle lc) throws Exception {
        JiniManager.setLocalOnly(true);
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.entering(this.getClass().getName(), "TxnManagerImpl", new Object[] { Arrays.asList(args),
                                                                                                 lc });
        }
        lifeCycle = lc;
        try {
            init(args);
        } catch (Throwable e) {
            cleanup();
            initFailed(e);
        }
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.exiting(this.getClass().getName(), "TxnManagerImpl");
        }
    }

    /** Initialization common to both activatable and transient instances. */
    private void init(String[] configArgs) throws Exception {
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.entering(this.getClass().getName(), "init", (Object[]) configArgs);
        }
        topUuid = UuidFactory.generate();

        if (JiniManager.isLocalOnly()) {
            final Configuration config = ConfigurationProvider.getInstance(configArgs, getClass().getClassLoader());
            doInit(config);
            List<Entry> entryList = new ArrayList<Entry>();
            Entry[] entries = (Entry[]) config.getEntry(TxnManager.MAHALO, "entries", Entry[].class, new Entry[] {});
            entryList.addAll(Arrays.asList(entries));
            ServiceItem serviceItem = new ServiceItem(new ServiceID(topUuid.getMostSignificantBits(),
                topUuid.getLeastSignificantBits()), this, (Entry[]) entryList.toArray(new Entry[entryList.size()]));
            LookupJiniAndLocal.addToLocalServices(serviceItem);

            if (operationsLogger.isLoggable(Level.FINER)) {
                operationsLogger.exiting(this.getClass().getName(), "init");
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private void doInit(Configuration config) throws Exception {
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.entering(this.getClass().getName(), "doInit", config);
        }

        // Create lease policy -- used by recovery logic, below??
        txnLeasePeriodPolicy = (LeasePeriodPolicy) Config.getNonNullEntry(config, TxnManager.MAHALO,
            "leasePeriodPolicy", LeasePeriodPolicy.class, new FixedLeasePeriodPolicy(3 * HOURS, 1 * HOURS));
        if (initLogger.isLoggable(Level.CONFIG)) {
            initLogger.log(Level.CONFIG, "leasePeriodPolicy is: {0}", txnLeasePeriodPolicy);
        }

        if (initLogger.isLoggable(Level.FINEST)) {
            initLogger.log(Level.FINEST, "Uuid is: {0}", topUuid);
        }

        if (initLogger.isLoggable(Level.FINEST)) {
            initLogger.log(Level.FINEST, "Exporting server");
        }
        serverStub = this;
        if (initLogger.isLoggable(Level.FINEST)) {
            initLogger.log(Level.FINEST, "Server stub: {0}", serverStub);
        }
        if (initLogger.isLoggable(Level.FINEST)) {
            initLogger.log(Level.FINEST, "Setting up data structures");
        }
        txns = Collections.synchronizedMap(new HashMap<Long, LocalTxnManagerTransaction>());

        // Used by log recovery logic
        settlerWakeupMgr = new WakeupManager(new WakeupManager.ThreadDesc(null, true));
        taskWakeupMgr = new WakeupManager(new WakeupManager.ThreadDesc(null, true));

        settlerpool = (TaskManager) Config.getNonNullEntry(config, TxnManager.MAHALO, "settlerPool", TaskManager.class,
            new TaskManager(settlerthreads, settlertimeout, settlerload));
        taskpool = (TaskManager) Config.getNonNullEntry(config, TxnManager.MAHALO, "taskPool", TaskManager.class,
            new TaskManager(taskthreads, tasktimeout, taskload));

        unsettledtxns = new Vector<Long>();

        // Create leaseFactory
        leaseFactory = new LeaseFactory(serverStub, topUuid);

        // Create LeaseExpirationMgr
        expMgr = new LeaseExpirationMgr(this);

        if (initLogger.isLoggable(Level.FINEST)) {
            initLogger.log(Level.FINEST, "Setting up log manager");
        }
        logmgr = new MultiLogManager();

        if (initLogger.isLoggable(Level.FINEST)) {
            initLogger.log(Level.FINEST, "Recovering state");
        }
        logmgr.recover();

        if (initLogger.isLoggable(Level.FINEST)) {
            initLogger.log(Level.FINEST, "Settling incomplete transactions");
        }
        settleThread = new InterruptedStatusThread("settleThread") {
            public void run() {
                try {
                    settleTxns();
                } catch (InterruptedException ie) {
                    if (transactionsLogger.isLoggable(Level.FINEST)) {
                        transactionsLogger.log(Level.FINEST, "settleThread interrupted -- exiting");
                    }
                    return;
                }
            };
        };
        settleThread.start();

        /*
         * With SecureRandom, the first ID requires generation of a secure seed,
         * which can take several seconds. We do it here so it doesn't affect
         * the first call's time. (I tried doing this in a separate thread so
         * some of the startup would occur during the roundtrip back the client,
         * but it didn't help much and this is simpler.)
         */
        nextID();

        if (startupLogger.isLoggable(Level.INFO)) {
            startupLogger.log(Level.INFO, "Mahalo started: {0}", this);
        }
        readyState.ready();

        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.exiting(this.getClass().getName(), "doInit");
        }
    }

    // TransactionManager interface method

    public TransactionManager.Created create(long lease) throws LeaseDeniedException {
        lease = Long.MAX_VALUE;
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.entering(this.getClass().getName(), "create", new Long(lease));
        }
        readyState.check();

        LocalTxnManagerTransaction txntr = null;

        long tid = nextID();
        Uuid uuid = createLeaseUuid(tid);

        if (transactionsLogger.isLoggable(Level.FINEST)) {
            transactionsLogger.log(Level.FINEST, "Transaction ID is: {0}", new Long(tid));
        }

        txntr = new LocalTxnManagerTransaction(serverStub, logmgr, tid, taskpool, taskWakeupMgr, this, uuid);
        Lease txnmgrlease = null;
        try {
            Result r = txnLeasePeriodPolicy.grant(txntr, lease);
            txntr.setExpiration(r.expiration);
            txnmgrlease = leaseFactory.newLease(uuid, r.expiration);
            expMgr.register(txntr);
        } catch (LeaseDeniedException lde) {
            // Should never happen in our implementation.
            throw new AssertionError("Transaction lease was denied" + lde);
        }

        if (transactionsLogger.isLoggable(Level.FINEST)) {
            transactionsLogger.log(Level.FINEST, "Created new LocalTxnManagerTransaction ID is: {0}", new Long(tid));
        }

        Transaction tr = txntr.getTransaction();
        ServerTransaction str = null;

        try {
            str = serverTransaction(tr);
            txns.put(new Long(str.id), txntr);

            if (transactionsLogger.isLoggable(Level.FINEST)) {
                transactionsLogger.log(Level.FINEST, "recorded new LocalTxnManagerTransaction", txntr);
            }

        } catch (Exception e) {
            if (transactionsLogger.isLoggable(Level.FINEST)) {
                transactionsLogger.log(Level.FINEST, "Problem creating transaction", e);
            }
            RuntimeException wrap = new RuntimeException("Unable to create transaction", e);
            transactionsLogger.throwing(this.getClass().getName(), "create", wrap);
            throw wrap;
        }

        TransactionManager.Created tmp = new TransactionManager.Created(str.id, txnmgrlease);
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.exiting(this.getClass().getName(), "create", tmp);
        }

        return tmp;
    }

    public void join(long id, TransactionParticipant part, long crashCount) throws UnknownTransactionException,
            CannotJoinException, CrashCountException, RemoteException {
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.entering(this.getClass().getName(), "join", new Object[] { new Long(id), part,
                                                                                       new Long(crashCount) });
        }
        readyState.check();

        LocalTxnManagerTransaction txntr = (LocalTxnManagerTransaction) txns.get(new Long(id));

        if (txntr == null)
            throw new UnknownTransactionException("unknown transaction");

        // txntr.join does expiration check
        txntr.join(part, crashCount);
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.exiting(this.getClass().getName(), "join");
        }
    }

    public int getState(long id) throws UnknownTransactionException {
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.entering(this.getClass().getName(), "getState", new Object[] { new Long(id) });
        }
        readyState.check();

        LocalTxnManagerTransaction txntr = (LocalTxnManagerTransaction) txns.get(new Long(id));

        if (txntr == null)
            throw new UnknownTransactionException("unknown transaction");
        /* Expiration checks are only meaningful for active transactions. */
        /*
         * NOTE: 1) Cancellation sets expiration to 0 without changing state
         * from Active right away. Clients are supposed to treat
         * UnknownTransactionException just like Aborted, so it's OK to send in
         * this case. 2) Might be a small window where client is committing the
         * transaction close to the expiration time. If the committed transition
         * takes place between getState() and ensureCurrent then the client
         * could get a false result.
         */
        // TODO - need better locking here. getState and expiration need to be
        // checked atomically
        int state = txntr.getState();
        if (state == ACTIVE && !ensureCurrent(txntr))
            throw new UnknownTransactionException("unknown transaction");

        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.exiting(this.getClass().getName(), "getState", new Integer(state));
        }
        return state;
    }

    public void commit(long id) throws UnknownTransactionException, CannotCommitException, RemoteException {
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.entering(this.getClass().getName(), "commit", new Long(id));
        }
        readyState.check();

        try {
            commit(id, 0);
        } catch (TimeoutExpiredException tee) {
            // This exception is swallowed because the
            // commit with no timeout only schedules a
            // roll-forward to happen
        }
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.exiting(this.getClass().getName(), "commit");
        }
    }

    public void commit(long id, long waitFor) throws UnknownTransactionException, CannotCommitException,
            TimeoutExpiredException, RemoteException {
        // !! No early return when not synchronous
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.entering(this.getClass().getName(), "commit", new Object[] { new Long(id),
                                                                                         new Long(waitFor) });
        }
        readyState.check();

        LocalTxnManagerTransaction txntr = (LocalTxnManagerTransaction) txns.get(new Long(id));

        if (transactionsLogger.isLoggable(Level.FINEST)) {
            transactionsLogger.log(Level.FINEST, "Retrieved LocalTxnManagerTransaction: {0}", txntr);
        }

        if (txntr == null)
            throw new UnknownTransactionException("Unknown transaction");

        // txntr.commit does expiration check
        txntr.commit(waitFor);
        txns.remove(new Long(id));

        if (transactionsLogger.isLoggable(Level.FINEST)) {
            transactionsLogger.log(Level.FINEST, "Committed transaction id {0}", new Long(id));
        }
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.exiting(this.getClass().getName(), "commit");
        }
    }

    public void abort(long id) throws UnknownTransactionException, CannotAbortException {
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.entering(this.getClass().getName(), "abort", new Object[] { new Long(id) });
        }
        readyState.check();
        try {
            abort(id, 0);
        } catch (TimeoutExpiredException tee) {
            // Swallow this exception because we only want to
            // schedule a settler task
        }
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.exiting(this.getClass().getName(), "abort");
        }
    }

    public void abort(long id, long waitFor) throws UnknownTransactionException, CannotAbortException,
            TimeoutExpiredException {
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.entering(this.getClass().getName(), "abort", new Object[] { new Long(id),
                                                                                        new Long(waitFor) });
        }
        readyState.check();

        // !! Multi-participants not supported
        // !! No early return when not synchronous

        // At this point, ask the Participants associated
        // with the Transaction to prepare

        LocalTxnManagerTransaction txntr = (LocalTxnManagerTransaction) txns.get(new Long(id));

        if (transactionsLogger.isLoggable(Level.FINEST)) {
            transactionsLogger.log(Level.FINEST, "Retrieved LocalTxnManagerTransaction: {0}", txntr);
        }
        /*
         * Since lease cancellation process sets expiration to 0 and then calls
         * abort, can't reliably check expiration at this point.
         */
        // TODO - Change internal, lease logic to call overload w/o expiration
        // check
        // TODO - Add expiration check to abort for external clients
        if (txntr == null)
            throw new CannotAbortException();

        txntr.abort(waitFor);
        txns.remove(new Long(id));

        if (transactionsLogger.isLoggable(Level.FINEST)) {
            transactionsLogger.log(Level.FINEST, "aborted transaction id {0}", new Long(id));
        }
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.exiting(this.getClass().getName(), "abort");
        }
    }

    // Satisfies the LogRecovery interface so that the
    // TransactionManager can recover it's non-transient
    // state in the face of process failure.

    /**
     * This method recovers state changes resulting from committing a
     * transaction. This re-creates the internal representation of the
     * transaction.
     * 
     * @param cookie
     *            the transaction's ID
     * 
     * @param rec
     *            the <code>LogRecord</code>
     */
    public void recover(long cookie, LogRecord rec) throws LogException {
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.entering(this.getClass().getName(), "recover", new Object[] { new Long(cookie), rec });
        }
        LocalTxnManagerTransaction tmt = enterTMT(cookie);
        LocalTxnLogRecord trec = (LocalTxnLogRecord) rec;
        try {
            trec.recover(tmt);
        } catch (CannotRecoverException e) {
            e.printStackTrace();
            System.exit(0);
        }
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.exiting(this.getClass().getName(), "recover");
        }
    }

    /**
     * Informs the transaction manager to attempt to settle a given transaction.
     * 
     * @param tid
     *            the transaction's ID
     */
    public synchronized void noteUnsettledTxn(long tid) {
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.entering(this.getClass().getName(), "noteUnsettledTxn", new Object[] { new Long(tid) });
        }
        unsettledtxns.add(new Long(tid));

        notifyAll();

        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.exiting(this.getClass().getName(), "noteUnsettledTxn");
        }
    }

    private synchronized void settleTxns() throws InterruptedException {
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.entering(this.getClass().getName(), "settleTxns");
        }
        if (transactionsLogger.isLoggable(Level.FINEST)) {
            transactionsLogger.log(Level.FINEST, "Settling {0} transactions.", new Integer(unsettledtxns.size()));
        }

        int numtxns = 0;
        Long first = null;
        long tid = 0;

        while (true) {
            numtxns = unsettledtxns.size();

            if (numtxns == 0) {
                if (transactionsLogger.isLoggable(Level.FINEST)) {
                    transactionsLogger.log(Level.FINEST, "Settler waiting");
                }
                wait();

                if (transactionsLogger.isLoggable(Level.FINEST)) {
                    transactionsLogger.log(Level.FINEST, "Settler notified");
                }
                continue;
            }

            first = null;

            first = (Long) unsettledtxns.firstElement();
            tid = first.longValue();

            SettlerTask task = new SettlerTask(settlerpool, settlerWakeupMgr, this, tid);
            settlerpool.add(task);
            unsettledtxns.remove(first);

            if (settleThread.hasBeenInterrupted())
                throw new InterruptedException("settleTxns interrupted");

            if (transactionsLogger.isLoggable(Level.FINEST)) {
                transactionsLogger.log(Level.FINEST, "Added SettlerTask for tid {0}", new Long(tid));
            }
        }
        // Not reachable
        /*
         * if (operationsLogger.isLoggable(Level.FINER)) {
         * operationsLogger.exiting(this.getClass().getName(), "settleTxns");
         */
    }

    // TransactionParticipant interface go here
    // when I implement nested transactions

    /**
     * Method from <code>TxnManager</code> which produces a
     * <code>Transaction</code> from its ID.
     * 
     * @param id
     *            the ID
     * 
     * @see net.jini.core.transaction.Transaction
     * @see com.sun.jini.mahalo.TxnManager
     */
    public Transaction getTransaction(long id) throws UnknownTransactionException {

        readyState.check();

        if (id == ((long) -1))
            return null;

        // First consult the hashtable for the Object
        // containing all actions performed under a
        // particular transaction

        LocalTxnManagerTransaction txntr = (LocalTxnManagerTransaction) txns.get(new Long(id));

        if (txntr == null)
            throw new UnknownTransactionException("unknown transaction");

        Transaction tn = (Transaction) txntr.getTransaction();
        ServerTransaction tr = serverTransaction(tn);

        if (tr == null)
            throw new UnknownTransactionException("TxnManagerImpl: getTransaction: " + "unable to find transaction("
                + id + ")");
        // TODO - use IDs vs equals
        if (!tr.mgr.equals(this))
            throw new UnknownTransactionException("wrong manager (" + tr.mgr + " instead of " + this + ")");

        return tr;
    }

    /**
     * Requests the renewal of a lease on a <code>Transaction</code>.
     * 
     * @param cookie
     *            identifies the leased resource
     * 
     * @param extension
     *            requested lease extension
     * 
     * @see net.jini.core.lease.Lease
     * @see com.sun.jini.landlord.LeasedResource
     * @see com.sun.jini.mahalo.LeaseManager
     */
    public long renew(Uuid uuid, long extension) throws UnknownLeaseException, LeaseDeniedException {

        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.entering(this.getClass().getName(), "renew", new Object[] { uuid, new Long(extension) });
        }
        readyState.check();

        verifyLeaseUuid(uuid);
        Long tid = getLeaseTid(uuid);
        LocalTxnManagerTransaction txntr = (LocalTxnManagerTransaction) txns.get(tid);

        if (txntr == null)
            throw new UnknownLeaseException();

        // synchronize on the resource so there is not a race condition
        // between renew and expiration
        Result r;
        synchronized (txntr) {
            // TODO - check for ACTIVE too?
            // TODO - if post-ACTIVE, do anything?
            if (!ensureCurrent(txntr))
                throw new UnknownLeaseException("Lease already expired");
            r = txnLeasePeriodPolicy.renew(txntr, extension);
            txntr.setExpiration(r.expiration);
            expMgr.renewed(txntr);
            if (operationsLogger.isLoggable(Level.FINER)) {
                operationsLogger.exiting(this.getClass().getName(), "renew", new Object[] { new Long(r.duration) });
            }
            return r.duration;
        }
    }

    /**
     * Cancels the lease on a <code>Transaction</code>.
     * 
     * @param cookie
     *            identifies the leased resource
     * 
     * @see net.jini.core.lease.Lease
     * @see com.sun.jini.landlord.LeasedResource
     * @see com.sun.jini.mahalo.LeaseManager
     */
    public void cancel(Uuid uuid) throws UnknownLeaseException {

        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.entering(this.getClass().getName(), "cancel", new Object[] { uuid });
        }
        readyState.check();

        verifyLeaseUuid(uuid);
        Long tid = getLeaseTid(uuid);
        LocalTxnManagerTransaction txntr = (LocalTxnManagerTransaction) txns.get(tid);

        if (txntr == null)
            throw new UnknownLeaseException();

        int state = txntr.getState();

        /**
         * Add this back in once LeaseExpirationManager uses an overloaded
         * version of cancel that doesn't perform an expiration check.
         * LeaseExpirationManager calls cancel() after the txn has expired, so
         * can't reliably check expiration here.
         * 
         * //TODO - need better locking here. getState and expiration need to be
         * checked atomically if ( (state == ACTIVE && !ensureCurrent(txntr)) ||
         * (state != ACTIVE)) throw new UnknownLeaseException("unknown
         * transaction");
         */

        if (state == ACTIVE) {

            synchronized (txntr) {
                txntr.setExpiration(0); // Mark as done
            }

            try {
                abort(((Long) tid).longValue());
            } catch (TransactionException e) {
                throw new UnknownLeaseException("When canceling abort threw:" + e.getClass().getName() + ":"
                    + e.getLocalizedMessage());
            }
        }

        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.exiting(this.getClass().getName(), "cancel");
        }
    }

    /**
     * Bulk renewal request of leases on <code>Transaction</code>s.
     * 
     * @param cookies
     *            identifies the leased resources
     * 
     * @param extensions
     *            requested lease extensions
     * 
     * @see net.jini.core.lease.Lease
     * @see com.sun.jini.landlord.LeasedResource
     * @see com.sun.jini.mahalo.LeaseManager
     */
    public Landlord.RenewResults renewAll(Uuid[] cookies, long[] extensions) {
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.entering(this.getClass().getName(), "renewAll");
        }
        readyState.check();

        Landlord.RenewResults results = LandlordUtil.renewAll(this, cookies, extensions);
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.exiting(this.getClass().getName(), "renewAll");
        }
        return results;
    }

    /**
     * Bulk cancel of leases on <code>Transaction</code>s.
     * 
     * @param cookies
     *            identifies the leased resources
     * 
     * @see net.jini.core.lease.Lease
     * @see com.sun.jini.landlord.LeasedResource
     * @see com.sun.jini.mahalo.LeaseManager
     */
    public Map cancelAll(Uuid[] cookies) {
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.entering(this.getClass().getName(), "cancelAll");
        }
        readyState.check();

        Map results = LandlordUtil.cancelAll(this, cookies);
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.exiting(this.getClass().getName(), "cancelAll");
        }
        return results;
    }

    // local methods

    /**
     * gets the next available transaction ID.
     */
    static long nextID() {
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.entering(LocalTransactionManager.class.getName(), "nextID");
        }
        long id;
        synchronized (idGen) {
            do {
                id = 0;
                idGen.nextBytes(idGenBuf);
                for (int i = 0; i < 8; i++)
                    id = (id << 8) | (idGenBuf[i] & 0xFF);
            } while (id == 0); // skip flag value
        }
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.exiting(LocalTransactionManager.class.getClass().getName(), "nextID", new Long(id));
        }
        return id;
    }

    private ServerTransaction serverTransaction(Transaction baseTr) throws UnknownTransactionException {
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.entering(this.getClass().getName(), "serverTransaction", baseTr);
        }
        try {
            if (operationsLogger.isLoggable(Level.FINER)) {
                operationsLogger.exiting(this.getClass().getName(), "serverTransaction", baseTr);
            }
            return (ServerTransaction) baseTr;
        } catch (ClassCastException e) {
            throw new UnknownTransactionException("unexpected transaction type");
        }
    }

    /**
     * Returns a reference to the <code>TransactionManager</code> interface.
     * 
     * @see net.jini.core.transaction.server.TransactionManager
     */
    public TransactionManager manager() {
        readyState.check();

        return serverStub;
    }

    private LocalTxnManagerTransaction enterTMT(long cookie) {
        Long key = new Long(cookie);
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.entering(this.getClass().getName(), "enterTMT", key);
        }
        LocalTxnManagerTransaction tmt = (LocalTxnManagerTransaction) txns.get(key);

        if (tmt == null) {
            Uuid uuid = createLeaseUuid(cookie);
            tmt = new LocalTxnManagerTransaction(serverStub, logmgr, cookie, taskpool, taskWakeupMgr, this, uuid);
            noteUnsettledTxn(cookie);
            /*
             * Since only aborted or committed txns are persisted, their
             * expirations are irrelevant. Therefore, any recovered transactions
             * are effectively lease.FOREVER.
             */
        }

        txns.put(key, tmt);

        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.exiting(this.getClass().getName(), "enterTMT", tmt);
        }
        return tmt;
    }

    // ***********************************************************
    // Admin

    // Methods required by DestroyAdmin

    /**
     * Cleans up and exits the transaction manager.
     */
    public void destroy() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the administration object for the transaction manager.
     */
    public Object getAdmin() {
        return this;
    }

    // ***********************************************************
    // Startup

    public Object getProxy() {
        return null;
    }

    /* inherit javadoc */
    public Object getServiceProxy() {
        return null;
    }

    /**
     * Log information about failing to initialize the service and rethrow the
     * appropriate exception.
     * 
     * @param e
     *            the exception produced by the failure
     */
    protected void initFailed(Throwable e) throws Exception {
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.entering(this.getClass().getName(), "initFailed");
        }
        if (initLogger.isLoggable(Level.SEVERE)) {
            initLogger.log(Level.SEVERE, "Mahalo failed to initialize", e);
        }
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.exiting(this.getClass().getName(), "initFailed");
        }
        if (e instanceof Exception) {
            throw (Exception) e;
        } else if (e instanceof Error) {
            throw (Error) e;
        } else {
            IllegalStateException ise = new IllegalStateException(e.getMessage());
            ise.initCause(e);
            throw ise;
        }
    }

    /*
	 * 
	 */
    private void cleanup() {
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.entering(this.getClass().getName(), "cleanup");
        }

        if (settlerpool != null) {
            if (initLogger.isLoggable(Level.FINEST)) {
                initLogger.log(Level.FINEST, "Terminating settlerpool.");
            }
            try {
                settlerpool.terminate();
                if (settlerWakeupMgr != null) {
                    if (initLogger.isLoggable(Level.FINEST)) {
                        initLogger.log(Level.FINEST, "Terminating settlerWakeupMgr.");
                    }
                    settlerWakeupMgr.stop();
                    settlerWakeupMgr.cancelAll();
                }
            } catch (Throwable t) {
                if (initLogger.isLoggable(Levels.HANDLED)) {
                    initLogger.log(Levels.HANDLED, "Trouble terminating settlerpool", t);
                }
            }
        }

        if (taskpool != null) {
            if (initLogger.isLoggable(Level.FINEST)) {
                initLogger.log(Level.FINEST, "Terminating taskpool.");
            }
            try {
                taskpool.terminate();
                if (taskWakeupMgr != null) {
                    if (initLogger.isLoggable(Level.FINEST)) {
                        initLogger.log(Level.FINEST, "Terminating taskWakeupMgr.");
                    }
                    taskWakeupMgr.stop();
                    taskWakeupMgr.cancelAll();
                }
            } catch (Throwable t) {
                if (initLogger.isLoggable(Levels.HANDLED)) {
                    initLogger.log(Levels.HANDLED, "Trouble terminating taskpool", t);
                }
            }
        }

        if (settleThread != null) {
            if (initLogger.isLoggable(Level.FINEST)) {
                initLogger.log(Level.FINEST, "Interrupting settleThread.");
            }
            try {
                settleThread.interrupt();
            } catch (Throwable t) {
                if (initLogger.isLoggable(Levels.HANDLED)) {
                    initLogger.log(Levels.HANDLED, "Trouble terminating settleThread", t);
                }
            }
        }

        if (expMgr != null) {
            if (initLogger.isLoggable(Level.FINEST)) {
                initLogger.log(Level.FINEST, "Terminating lease expiration manager.");
            }
            expMgr.terminate();
        }

        if (initLogger.isLoggable(Level.FINEST)) {
            initLogger.log(Level.FINEST, "Destroying JoinStateManager.");
        }

        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.exiting(this.getClass().getName(), "cleanup");
        }
    }

    // ////////////////////////////////////////
    // ProxyTrust Method
    // ////////////////////////////////////////
    public TrustVerifier getProxyVerifier() {
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.exiting(this.getClass().getName(), "getProxyVerifier");
        }
        readyState.check();

        throw new UnsupportedOperationException();
    }

    /**
     * Utility method that check for valid resource
     */
    private static boolean ensureCurrent(LeasedResource resource) {
        return resource.getExpiration() > System.currentTimeMillis();
    }

    /*
     * Attempt to build "real" Uuid from topUuid.getLeastSignificantBits(),
     * which contains the variant field, and the transaction id, which should be
     * unique for this service. Between the two of these, the Uuid should be
     * unique.
     */
    private Uuid createLeaseUuid(long txnId) {
        return UuidFactory.create(topUuid.getLeastSignificantBits(), txnId);
    }

    private void verifyLeaseUuid(Uuid uuid) throws UnknownLeaseException {
        /*
         * Note: Lease Uuid contains - Most Sig => the least sig bits of topUuid
         * -
         * Least Sig => the txn id
         */
        // Check to if this server granted the resource
        if (uuid.getMostSignificantBits() != topUuid.getLeastSignificantBits()) {
            throw new UnknownLeaseException();
        }

    }

    private Long getLeaseTid(Uuid uuid) {
        // Extract the txn id from the lower bits of the uuid
        return new Long(uuid.getLeastSignificantBits());
    }

    public void addLookupAttributes(Entry[] arg0) throws RemoteException {
        // TODO Auto-generated method stub

    }

    public void addLookupGroups(String[] arg0) throws RemoteException {
        // TODO Auto-generated method stub

    }

    public void addLookupLocators(LookupLocator[] arg0) throws RemoteException {
        // TODO Auto-generated method stub

    }

    public Entry[] getLookupAttributes() throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    public String[] getLookupGroups() throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    public LookupLocator[] getLookupLocators() throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    public void modifyLookupAttributes(Entry[] arg0, Entry[] arg1) throws RemoteException {
        // TODO Auto-generated method stub

    }

    public void removeLookupGroups(String[] arg0) throws RemoteException {
        // TODO Auto-generated method stub

    }

    public void removeLookupLocators(LookupLocator[] arg0) throws RemoteException {
        // TODO Auto-generated method stub

    }

    public void setLookupGroups(String[] arg0) throws RemoteException {
        // TODO Auto-generated method stub

    }

    public void setLookupLocators(LookupLocator[] arg0) throws RemoteException {
        // TODO Auto-generated method stub

    }
}
