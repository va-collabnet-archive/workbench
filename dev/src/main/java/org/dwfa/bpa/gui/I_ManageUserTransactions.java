/*
 * Created on Jan 30, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
 */
package org.dwfa.bpa.gui;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.rmi.RemoteException;
import java.security.PrivilegedActionException;
import java.util.Collection;

import net.jini.core.lease.LeaseDeniedException;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;


public interface I_ManageUserTransactions {

    public void addActiveTransactionListener(PropertyChangeListener listener);
    public void removeActiveTransactionListener(PropertyChangeListener listener);
    
    public void addUncommittedComponentsListener(PropertyChangeListener listener);
    public void removeUncommittedComponentsListener(PropertyChangeListener listener);
    
    public void commitActiveTransaction() throws TransactionException, RemoteException;
    
    public Transaction getActiveTransaction() throws TransactionException, LeaseDeniedException, RemoteException, InterruptedException, IOException, PrivilegedActionException;

    public boolean isTransactionActive() throws TransactionException;
    
    public void setActiveTransaction(Transaction activeTransaction)
    throws TransactionException;
    
    public void setTransactionDuration(long transactionDuration);
    public Collection<?> getUncommittedComponents() throws TransactionException;
    public void abortActiveTransaction() throws TransactionException, RemoteException;
}
