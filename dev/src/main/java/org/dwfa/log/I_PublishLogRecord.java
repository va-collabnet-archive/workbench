/*
 * Created on Apr 24, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.log;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.logging.LogRecord;

import net.jini.id.Uuid;

/**
 * @author kec
 *
 */
public interface I_PublishLogRecord extends Remote {
    /**
     * @see java.util.logging.Handler#publish(java.util.logging.LogRecord)
     */
    public void publish(LogRecord record) throws RemoteException;
    
    public Uuid getId() throws RemoteException;
}