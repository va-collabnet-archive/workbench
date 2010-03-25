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
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import net.jini.id.Uuid;

/**
 * @author kec
 * 
 */
public class RemoteHandlerAdaptor extends Handler {
    I_PublishLogRecord remotePublisher;
    I_ManageLogs logManager;
    String loggerName;

    /**
     * 
     */
    public RemoteHandlerAdaptor(I_PublishLogRecord remotePublisher, String loggerName, I_ManageLogs logManager) {
        super();
        this.remotePublisher = remotePublisher;
        this.logManager = logManager;
        this.loggerName = loggerName;
    }

    /**
     * @see java.util.logging.Handler#publish(java.util.logging.LogRecord)
     */
    public void publish(LogRecord logRec) {
        try {
            this.remotePublisher.publish(logRec);
        } catch (RemoteException e) {
            e.printStackTrace();
            try {
                logManager.removeRemoteHandler(loggerName, remotePublisher.getId());
            } catch (RemoteException e1) {
                e1.printStackTrace();
            }
        }

    }

    /**
     * @see java.util.logging.Handler#flush()
     */
    public void flush() {

    }

    /**
     * @see java.util.logging.Handler#close()
     */
    public void close() throws SecurityException {

    }

    /**
     * @return
     * @throws RemoteException
     */
    public Uuid getId() throws RemoteException {
        return remotePublisher.getId();
    }
}
