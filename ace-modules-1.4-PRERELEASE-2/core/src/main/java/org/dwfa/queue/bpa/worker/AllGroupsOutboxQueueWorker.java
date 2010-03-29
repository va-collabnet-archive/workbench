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
 * Created on Jul 2, 2005
 */
package org.dwfa.queue.bpa.worker;

import java.io.IOException;
import java.rmi.RemoteException;
import java.security.PrivilegedActionException;
import java.util.UUID;

import javax.security.auth.login.LoginException;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.lookup.ServiceItemFilter;

import org.dwfa.bpa.process.I_SelectProcesses;

/**
 * @author kec
 * 
 */
public class AllGroupsOutboxQueueWorker extends OutboxQueueWorker {

    /**
     * @param config
     * @param id
     * @param desc
     * @param selector
     * @throws ConfigurationException
     * @throws LoginException
     * @throws IOException
     */
    public AllGroupsOutboxQueueWorker(Configuration config, UUID id, String desc, I_SelectProcesses selector)
            throws ConfigurationException, LoginException, IOException, PrivilegedActionException {
        super(config, id, desc, selector);
    }

    /**
     * @param tmpl
     * @param filter
     * @return
     * @throws InterruptedException
     * @throws RemoteException
     * @throws IOException
     * @throws ConfigurationException
     * @throws PrivilegedActionException
     */
    protected ServiceItem jinilookup(ServiceTemplate tmpl, ServiceItemFilter filter) throws InterruptedException,
            RemoteException, IOException, PrivilegedActionException, ConfigurationException {
        ServiceItem[] items = this.lookupAllGroups(tmpl, 1, 1, filter, 1000 * 10);
        if (items == null || items.length < 1) {
            return null;
        }
        return items[0];
    }
}
