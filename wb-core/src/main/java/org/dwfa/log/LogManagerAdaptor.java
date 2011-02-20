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
    public List<String> getLoggerNames() {
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
    public Object getLevel(String loggerName) {
        if (this.logManager == null || this.logManager.getLogger(loggerName) == null) {
            return null;
        }
        return this.logManager.getLogger(loggerName).getLevel();
    }

    /**
     * @see org.dwfa.log.I_ManageLogs#isLoggable(java.lang.String,
     *      java.util.logging.Level)
     */
    public boolean isLoggable(String loggerName, Level level) {
        if (this.logManager.getLogger(loggerName) == null) {
            return false;
        }
        return this.logManager.getLogger(loggerName).isLoggable(level);
    }

    /**
     * @throws RemoteException
     * @see org.dwfa.log.I_ManageLogs#addRemoteHandler(java.lang.String,
     *      org.dwfa.log.I_PublishLogRecord)
     */
    public boolean addRemoteHandler(String loggerName, I_PublishLogRecord remoteHandler) throws RemoteException {
        RemoteHandlerAdaptor adaptor = new RemoteHandlerAdaptor(remoteHandler, loggerName, this);
        if (this.remoteHandlerAdaptors.containsKey(adaptor.getId() + loggerName)) {
            return false;
        }
        this.remoteHandlerAdaptors.put(adaptor.getId() + loggerName, adaptor);
        this.logManager.getLogger(loggerName).addHandler(adaptor);
        return true;
    }

    /**
     * @see org.dwfa.log.I_ManageLogs#removeRemoteHandler(java.lang.String,
     *      net.jini.id.Uuid)
     */
    public void removeRemoteHandler(String loggerName, Uuid id) {
        RemoteHandlerAdaptor adaptor = (RemoteHandlerAdaptor) this.remoteHandlerAdaptors.remove(id + loggerName);
        this.logManager.getLogger(loggerName).removeHandler(adaptor);
    }
}
