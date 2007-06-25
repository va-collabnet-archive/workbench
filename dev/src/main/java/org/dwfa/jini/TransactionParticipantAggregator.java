/*
 * Created on Jan 13, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.jini;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.config.ConfigurationProvider;
import net.jini.core.transaction.CannotJoinException;
import net.jini.core.transaction.TransactionException;
import net.jini.core.transaction.UnknownTransactionException;
import net.jini.core.transaction.server.CrashCountException;
import net.jini.core.transaction.server.ServerTransaction;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.core.transaction.server.TransactionParticipant;
import net.jini.export.Exporter;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.tcp.TcpServerEndpoint;

import com.sun.jini.start.LifeCycle;

/**
 * Allows changes to be
 * governed by Jini transactions.
 * 
 * @author kec
 *  
 */
public class TransactionParticipantAggregator implements
		TransactionParticipant {
	Map<ServerTransaction, List<I_TransactionPart>> transactionMap = new HashMap<ServerTransaction, List<I_TransactionPart>>();

	private static TransactionParticipantAggregator singleton;

	private static TransactionParticipant proxy;

	private Set<ActionListener> listeners = new HashSet<ActionListener>();

	protected static Logger logger = Logger.getLogger(TransactionParticipantAggregator.class.getName());

	private Configuration config;
    
    @SuppressWarnings("unused")
	private LifeCycle lifeCycle = null;


	protected TransactionParticipantAggregator(String[] args, LifeCycle lc)
			throws Exception {
		logger.info("\n*******************\n\n" + 
				"Starting service with config file: " + Arrays.asList(args) +
				"\n\n******************\n");
		this.config = ConfigurationProvider.getInstance(
                args, getClass().getClassLoader());
        this.lifeCycle = lc;
        singleton = this;
        
        this.init();
        logger.info("TransactionParticipantAggregator is ready");
        logger.exiting(this.getClass().getName(), "<init>");

	}

	private void init() throws Exception {
		LoginContext loginContext = (LoginContext) config
				.getEntry(this.getClass().getName(), "loginContext",
						LoginContext.class, null);
		if (JiniManager.isLocalOnly()) {
			proxy = this;
		} else {
			if (loginContext == null) {
				initAsSubject();
			} else {
				loginContext.login();
				Subject.doAsPrivileged(loginContext.getSubject(),
						new PrivilegedExceptionAction() {
							public Object run() throws Exception {
								initAsSubject();
								return null;
							}
						}, null);
			}
		}

	}

	/**
	 * Initializes the server, assuming that the appropriate subject is in
	 * effect.
	 */
	protected void initAsSubject() throws Exception {
		/* Export the server */
		Exporter exporter = getExporter();
		Remote backend = exporter.export(this);

		/* Create the smart proxy */
		proxy = TransactionParticipantProxy
				.create((TransactionParticipant) backend);
	}

	/**
	 * Returns the exporter for exporting the server.
	 * 
	 * @throws ConfigurationException
	 *             if a problem occurs getting the exporter from the
	 *             configuration
	 * @throws RemoteException
	 *             if a remote communication problem occurs
	 */
	protected Exporter getExporter() throws ConfigurationException,
			RemoteException {
		return (Exporter) config
				.getEntry(this.getClass().getName(), "exporter",
						Exporter.class, new BasicJeriExporter(TcpServerEndpoint
								.getInstance(0), new BasicILFactory()));
	}


	public static void addCommitListener(ActionListener listener)
			throws Exception {
		checkInitialized();
		singleton.listeners.add(listener);

	}

	public static void removeCommitListener(ActionListener listener)
			throws Exception {
		checkInitialized();
		singleton.listeners.remove(listener);

	}
    
    
	/**
	 * @param st
	 * @param part
	 * @throws UnknownTransactionException
	 * @throws CannotJoinException
	 * @throws CrashCountException
	 * @throws RemoteException
	 */
	public synchronized static void addTransactionPart(ServerTransaction st,
			I_TransactionPart part) throws UnknownTransactionException,
			CannotJoinException, CrashCountException, RemoteException {
		List<I_TransactionPart> partList = singleton.transactionMap.get(st);
		if (partList == null) {
            partList = new ArrayList<I_TransactionPart>();
			singleton.transactionMap.put(st, partList);
			st.join(proxy, 0);
		}
		partList.add(part);
	}

	/**
	 * @throws TransactionException
	 */
	private synchronized static void checkInitialized() throws TransactionException {
		if (TransactionParticipantAggregator.singleton == null) {
			throw new TransactionException(
					"TransactionParticipantAggregator not initialized");
		}
	}

	/**
	 * @see net.jini.core.transaction.server.TransactionParticipant#prepare(net.jini.core.transaction.server.TransactionManager,
	 *      long)
	 */
	public synchronized int prepare(TransactionManager mgr, long id)
			throws UnknownTransactionException, RemoteException {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Preparing " + id);
        }
		return PREPARED;
	}

	/**
	 * @see net.jini.core.transaction.server.TransactionParticipant#commit(net.jini.core.transaction.server.TransactionManager,
	 *      long)
	 */
	public synchronized void commit(TransactionManager mgr, long id)
			throws UnknownTransactionException, RemoteException {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Starting commit for " + id);
        }
		ServerTransaction st = new ServerTransaction(mgr, id);
		List partsList = this.transactionMap.remove(st);
		if (partsList == null) {
			throw new UnknownTransactionException(
					"Not contained in transactionMap");
		}
		Date commitDate = null;
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Committing " + partsList.size() + " parts.");
        }
		for (Iterator setItr = partsList.iterator(); setItr.hasNext();) {
			I_TransactionPart part = (I_TransactionPart) setItr.next();
			if (commitDate == null) {
                if (logger.isLoggable(Level.FINER)) {
                    logger.finer("getting commitDate for: " + id);
                }
                commitDate = new Date();
                if (logger.isLoggable(Level.FINER)) {
                    logger.finer("getting commitDate for: " + id + " is: " + commitDate);
                }
			}
            if (logger.isLoggable(Level.FINER)) {
                logger.finer("commiting part: " + part);
            }
			part.commit(mgr, id, commitDate);
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("committed part: " + part);
            }
		}
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Finished commit for " + id);
        }
		for (Iterator<ActionListener> itr = this.listeners.iterator(); itr.hasNext();) {
			ActionEvent event = new ActionEvent(this, 0,
					"Transaction committed");
			ActionListener listener = itr.next();
            if (logger.isLoggable(Level.FINER)) {
                logger.finer("Notifying: " + listener);
            }
			listener.actionPerformed(event);
		}
	}

	/**
	 * @see net.jini.core.transaction.server.TransactionParticipant#abort(net.jini.core.transaction.server.TransactionManager,
	 *      long)
	 */
	public synchronized void abort(TransactionManager mgr, long id)
			throws UnknownTransactionException, RemoteException {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Starting abort for " + id);
        }
		ServerTransaction st = new ServerTransaction(mgr, id);
		List partsList = this.transactionMap.remove(st);
		if (partsList == null) {
			throw new UnknownTransactionException(
					"Not contained in transactionMap");
		}
		for (Iterator i = partsList.iterator(); i.hasNext();) {
			I_TransactionPart part = (I_TransactionPart) i.next();
			part.abort(mgr, id);
		}
        if (logger.isLoggable(Level.FINEST)) {
            logger.fine("Finished abort for " + id);
        }
	}

	/**
	 * @see net.jini.core.transaction.server.TransactionParticipant#prepareAndCommit(net.jini.core.transaction.server.TransactionManager,
	 *      long)
	 */
	public synchronized int prepareAndCommit(TransactionManager mgr, long id)
			throws UnknownTransactionException, RemoteException {
		this.commit(mgr, id);
		return COMMITTED;
	}

	/**
	 * @return Returns the logger.
	 */
	public static Logger getLogger() {
		return logger;
	}

}