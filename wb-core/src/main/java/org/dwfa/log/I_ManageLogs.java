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
