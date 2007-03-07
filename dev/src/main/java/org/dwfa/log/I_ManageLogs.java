/*
 * Created on Apr 24, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.log;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.logging.Level;

import net.jini.id.Uuid;

/**
 * @author kec
 *
 */
public interface I_ManageLogs extends Remote {

    /**
     * @return
     */
    List<String> getLoggerNames() throws RemoteException;


    /**
     * @param loggerName
     * @return
     */
    Object getLevel(String loggerName) throws RemoteException;

    /**
     * @param loggerName
     * @param level
     * @return
     */
    boolean isLoggable(String loggerName, Level level) throws RemoteException;
    
    public boolean addRemoteHandler(String loggerName, I_PublishLogRecord remoteHandler) throws RemoteException;

    public void removeRemoteHandler(String loggerName, Uuid id) throws RemoteException;

}
