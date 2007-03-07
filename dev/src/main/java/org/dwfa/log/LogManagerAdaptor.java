/*
 * Created on Apr 24, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.log;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;

import net.jini.id.Uuid;

/**
 * @author kec
 *
 */
public class LogManagerAdaptor implements I_ManageLogs {
    LogManager logManager;
    Map<String, RemoteHandlerAdaptor> remoteHandlerAdaptors = new HashMap<String, RemoteHandlerAdaptor>();
    /**
     * 
     */
    public LogManagerAdaptor(LogManager logManager) {
        super();
        this.logManager = logManager;
    }

    /**
     * @see org.dwfa.log.I_ManageLogs#getLoggerNames()
     */
    public List<String> getLoggerNames()  {
        List<String> nameList = new ArrayList<String>();
        Enumeration<String> nameEnum = this.logManager.getLoggerNames();
        while (nameEnum.hasMoreElements()) {
            nameList.add(nameEnum.nextElement());
        }
        return nameList;
    }

    /**
     * @see org.dwfa.log.I_ManageLogs#getLevel(java.lang.String)
     */
    public Object getLevel(String loggerName)  {
        return this.logManager.getLogger(loggerName).getLevel();
    }

    /**
     * @see org.dwfa.log.I_ManageLogs#isLoggable(java.lang.String, java.util.logging.Level)
     */
    public boolean isLoggable(String loggerName, Level level) {
        return this.logManager.getLogger(loggerName).isLoggable(level);
    }

    /**
     * @throws RemoteException
     * @see org.dwfa.log.I_ManageLogs#addRemoteHandler(java.lang.String, org.dwfa.log.I_PublishLogRecord)
     */
    public boolean addRemoteHandler(String loggerName, I_PublishLogRecord remoteHandler) throws RemoteException {
        RemoteHandlerAdaptor adaptor = new RemoteHandlerAdaptor(remoteHandler, loggerName, this);
        if (this.remoteHandlerAdaptors.containsKey(adaptor.getId()+loggerName)) {
            return false;
        }
        this.remoteHandlerAdaptors.put(adaptor.getId()+loggerName, adaptor);
        this.logManager.getLogger(loggerName).addHandler(adaptor);
        return true;
    }

    /**
     * @see org.dwfa.log.I_ManageLogs#removeRemoteHandler(java.lang.String, net.jini.id.Uuid)
     */
    public void removeRemoteHandler(String loggerName, Uuid id) {
        RemoteHandlerAdaptor adaptor = (RemoteHandlerAdaptor) this.remoteHandlerAdaptors.remove(id+loggerName);
        this.logManager.getLogger(loggerName).removeHandler(adaptor);
    }
}
