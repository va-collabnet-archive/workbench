/*
 * Created on Mar 30, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.jini;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.lookup.ServiceDiscoveryManager;
import net.jini.lookup.ServiceItemFilter;

/**
 * @author kec
 *  
 */
public class LookupJiniAndLocal implements I_LookupServices {

    private static Logger logger = Logger.getLogger(LookupJiniAndLocal.class.getName());

    private static List<ServiceItem> localServices = Collections.synchronizedList(new ArrayList<ServiceItem>());

    ServiceDiscoveryManager sdm;

    /**
     *  
     */
    public LookupJiniAndLocal(ServiceDiscoveryManager sdm) {
        super();
        this.sdm = sdm;
    }

    /**
     * @see com.informatics.jini.I_LookupServices#lookup(net.jini.core.lookup.ServiceTemplate,
     *      int, int, net.jini.lookup.ServiceItemFilter, long)
     */
    public ServiceItem[] lookup(ServiceTemplate tmpl, int minMatches,
            int maxMatches, ServiceItemFilter filter, long waitDur)
            throws InterruptedException, RemoteException {
        Collection<ServiceItem> matches = this.lookupLocal(tmpl, filter);
        matches.addAll(Arrays.asList(this.sdm.lookup(tmpl, minMatches, maxMatches - matches.size(), filter, waitDur)));
        return (ServiceItem[]) matches.toArray(new ServiceItem[matches.size()]);
    }

    /**
     * @see com.informatics.jini.I_LookupServices#lookup(net.jini.core.lookup.ServiceTemplate,
     *      int, net.jini.lookup.ServiceItemFilter)
     */
    public ServiceItem[] lookup(ServiceTemplate tmpl, int maxMatches,
            ServiceItemFilter filter) {
        Collection<ServiceItem> matches = this.lookupLocal(tmpl, filter);
        ServiceItem[] jiniMatches = this.sdm.lookup(tmpl, maxMatches - matches.size(), filter);
        matches.addAll(Arrays.asList(jiniMatches));
        return (ServiceItem[]) matches.toArray(new ServiceItem[matches.size()]);
    }

    /**
     * @see com.informatics.jini.I_LookupServices#lookup(net.jini.core.lookup.ServiceTemplate,
     *      net.jini.lookup.ServiceItemFilter)
     */
    public ServiceItem lookup(ServiceTemplate tmpl, ServiceItemFilter filter) {
        List<ServiceItem> matches = this.lookupLocal(tmpl, filter);
        if (matches.size() > 0) {
            return matches.get(0);
        }
        return this.sdm.lookup(tmpl,  filter);
    }

    /**
     * @see com.informatics.jini.I_LookupServices#lookup(net.jini.core.lookup.ServiceTemplate,
     *      net.jini.lookup.ServiceItemFilter, long)
     */
    public ServiceItem lookup(ServiceTemplate tmpl, ServiceItemFilter filter,
            long waitDur) throws InterruptedException, RemoteException {
        List<ServiceItem> matches = this.lookupLocal(tmpl, filter);
        if (matches.size() > 0) {
            return matches.get(0);
        }
        return this.sdm.lookup(tmpl, filter, waitDur);
    }

    private List<ServiceItem> lookupLocal(ServiceTemplate tmpl,
            ServiceItemFilter filter) {
        List<ServiceItem> matches = new ArrayList<ServiceItem>();
        synchronized(LookupJiniAndLocal.localServices) {
        for (Iterator<ServiceItem> itr = LookupJiniAndLocal.localServices.iterator(); itr.hasNext();) {
            ServiceItem serviceItem = itr.next();
            if ((checkServiceId(tmpl, serviceItem) && checkServiceTypes(tmpl, serviceItem)
                    && checkEntries(tmpl, serviceItem))) {
                if ((filter == null) || (filter.check(serviceItem))) {
                    matches.add(serviceItem);
                }
            }
        }
        }
        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, "Local services found: " + matches);
        }
        return matches;
    }

    /**
     * @param tmpl
     * @param serviceItem
     */
    private boolean checkServiceId(ServiceTemplate tmpl, ServiceItem serviceItem) {
        if (tmpl.serviceID == null) {
            return true;
        }
        if (tmpl.serviceID.equals(serviceItem.serviceID)) {
            return true;
        }
        return false;
    }
    /**
     * @param tmpl
     * @param serviceItem
     */
    @SuppressWarnings("unchecked")
    private boolean checkServiceTypes(ServiceTemplate tmpl, ServiceItem serviceItem) {
        if (tmpl.serviceTypes == null) {
            return true;
        }
        for (int i = 0; i < tmpl.serviceTypes.length; i++) {
            if (tmpl.serviceTypes[i].isAssignableFrom(serviceItem.service
                    .getClass()) == false) {
                return false;
            }
        }
        return true;
    }
    private boolean checkEntries(ServiceTemplate tmpl, ServiceItem serviceItem) {
        if (tmpl.attributeSetTemplates == null) {
            return true;
        }
        if (tmpl.attributeSetTemplates.length == 0) {
            return true;
        }
        for (int i = 0; i < tmpl.attributeSetTemplates.length; i++) {
            boolean found = false;
            for (int j = 0; j < serviceItem.attributeSets.length; j++) {
                if (tmpl.attributeSetTemplates[i].equals(serviceItem.attributeSets[j])) {
                    found = true;
                    break;
                }
                if (found == false) {
                    return false;
                }
            }
        }
        return true;
    }

    public void addLocalService(ServiceItem serviceItem) {
        LookupJiniAndLocal.addToLocalServices(serviceItem);
    }
    
    public static void addToLocalServices(ServiceItem serviceItem) {
        LookupJiniAndLocal.localServices.add(serviceItem);
    }

}