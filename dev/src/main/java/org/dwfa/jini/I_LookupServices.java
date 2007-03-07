/*
 * Created on Mar 30, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
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
    public ServiceItem[] lookup(ServiceTemplate tmpl, int minMatches,
            int maxMatches, ServiceItemFilter filter, long waitDur)
            throws InterruptedException, RemoteException;

    /**
     * @param tmpl
     * @param maxMatches
     * @param filter
     * @return
     */
    public ServiceItem[] lookup(ServiceTemplate tmpl, int maxMatches,
            ServiceItemFilter filter);

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
    public ServiceItem lookup(ServiceTemplate tmpl, ServiceItemFilter filter,
            long waitDur) throws InterruptedException, RemoteException;
    
    public void addLocalService(ServiceItem service);

}