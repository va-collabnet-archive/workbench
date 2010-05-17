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
 * Created on Mar 30, 2005
 */
package org.dwfa.jini;

import java.rmi.RemoteException;

import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.lookup.ServiceItemFilter;

/**
 * @author kec
 * 
 */
public interface I_LookupServices {
    /**
     * @param tmpl
     * @param minMatches
     * @param maxMatches
     * @param filter
     * @param waitDur
     * @return
     * @throws java.lang.InterruptedException
     * @throws java.rmi.RemoteException
     */
    public ServiceItem[] lookup(ServiceTemplate tmpl, int minMatches, int maxMatches, ServiceItemFilter filter,
            long waitDur) throws InterruptedException, RemoteException;

    /**
     * @param tmpl
     * @param maxMatches
     * @param filter
     * @return
     */
    public ServiceItem[] lookup(ServiceTemplate tmpl, int maxMatches, ServiceItemFilter filter);

    /**
     * @param tmpl
     * @param filter
     * @return
     */
    public ServiceItem lookup(ServiceTemplate tmpl, ServiceItemFilter filter);

    /**
     * @param tmpl
     * @param filter
     * @param waitDur
     * @return
     * @throws java.lang.InterruptedException
     * @throws java.rmi.RemoteException
     */
    public ServiceItem lookup(ServiceTemplate tmpl, ServiceItemFilter filter, long waitDur)
            throws InterruptedException, RemoteException;

    public void addLocalService(ServiceItem service);

}
