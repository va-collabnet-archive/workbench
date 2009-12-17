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
 * Created on Mar 3, 2005
 */
package org.dwfa.jini;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.logging.Logger;

import net.jini.core.discovery.LookupLocator;
import net.jini.core.lease.Lease;
import net.jini.core.lease.LeaseDeniedException;
import net.jini.core.lease.UnknownLeaseException;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionFactory;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.discovery.DiscoveryGroupManagement;
import net.jini.discovery.DiscoveryListener;
import net.jini.discovery.LookupDiscoveryManager;
import net.jini.lease.LeaseListener;
import net.jini.lease.LeaseRenewalManager;
import net.jini.lookup.LookupCache;
import net.jini.lookup.ServiceDiscoveryListener;
import net.jini.lookup.ServiceDiscoveryManager;
import net.jini.lookup.ServiceItemFilter;

/**
 * @author kec
 * 
 */
public class JiniManager implements I_LookupServices {
    public static Logger logger = Logger.getLogger(JiniManager.class.getName());

    private static boolean localOnly = false;

    private static boolean lookupInCache = true;

    private static JiniManager singelton;

    // private
    private ServiceDiscoveryManager sdm;

    private ServiceDiscoveryManager allGroupSdm;

    private LookupJiniAndLocal jiniAndLocal;

    private LookupCache transactionManagerCache;

    private LeaseRenewalManager leaseRenewalManager;

    private int renewDuration = 30 * 1000; // renew every 30 seconds

    public static JiniManager getLocalOnlyJiniManager() throws IOException {
        if (singelton == null) {
            singelton = new JiniManager(null);
        }
        return singelton;
    }

    /**
     * @throws IOException
     * 
     */
    public JiniManager(ServiceDiscoveryManager sdm) throws IOException {
        logger.info("Starting JiniManager: " + sdm);
        this.leaseRenewalManager = new LeaseRenewalManager();
        this.sdm = sdm;

        if (localOnly == false) {

            LookupLocator[] lookupLocators = new LookupLocator[0];
            DiscoveryListener discoveryListener = null;

            LookupDiscoveryManager allGroupsLookupDiscoveryManager = new LookupDiscoveryManager(
                DiscoveryGroupManagement.ALL_GROUPS, lookupLocators, discoveryListener);
            this.allGroupSdm = new ServiceDiscoveryManager(allGroupsLookupDiscoveryManager, leaseRenewalManager);

            ServiceTemplate tmpl = new ServiceTemplate(null, new Class[] { TransactionManager.class }, null);
            ServiceItemFilter filter = null;
            ServiceListModel serviceListener = new ServiceListModel();
            this.transactionManagerCache = this.createLookupCache(tmpl, filter, serviceListener);
        }
        this.jiniAndLocal = new LookupJiniAndLocal(this.sdm);

    }

    /**
     * @param tmpl
     * @param filter
     * @param listener
     * @return
     * @throws java.rmi.RemoteException
     */
    public LookupCache createLookupCache(ServiceTemplate tmpl, ServiceItemFilter filter,
            ServiceDiscoveryListener listener) throws RemoteException {
        return sdm.createLookupCache(tmpl, filter, listener);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        return sdm.equals(obj);
    }

    @Override
    public int hashCode() {
        if (sdm == null) {
            return super.hashCode();
        }
        return sdm.hashCode();
    }

    /**
     * @return
     */
    public LookupDiscoveryManager getLookupDiscoveryManager() {
        return (LookupDiscoveryManager) sdm.getDiscoveryManager();
    }

    /**
     * @return
     */
    public ServiceDiscoveryManager getAllGroupsServiceDiscoveryManager() {
        if (localOnly) {
            throw new UnsupportedOperationException();
        }
        return this.allGroupSdm;
    }

    /**
     * @return
     */
    public LeaseRenewalManager getLeaseRenewalManager() {
        return sdm.getLeaseRenewalManager();
    }

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
            long waitDur) throws InterruptedException, RemoteException {
        return lookup(tmpl, minMatches, maxMatches, filter, waitDur, false);
    }

    public ServiceItem[] lookup(ServiceTemplate tmpl, int minMatches, int maxMatches, ServiceItemFilter filter,
            long waitDur, boolean lookupLocal) throws InterruptedException, RemoteException {
        if (lookupLocal || localOnly || lookupInCache) {
            return jiniAndLocal.lookup(tmpl, minMatches, maxMatches, filter, waitDur);
        } else {
            return sdm.lookup(tmpl, minMatches, maxMatches, filter, waitDur);
        }
    }

    /**
     * @param tmpl
     * @param maxMatches
     * @param filter
     * @return
     */
    public ServiceItem[] lookup(ServiceTemplate tmpl, int maxMatches, ServiceItemFilter filter) {
        return this.lookup(tmpl, maxMatches, filter, false);
    }

    public ServiceItem[] lookup(ServiceTemplate tmpl, int maxMatches, ServiceItemFilter filter, boolean lookupLocal) {
        if (lookupLocal || localOnly || lookupInCache) {
            return jiniAndLocal.lookup(tmpl, maxMatches, filter);
        }
        return this.sdm.lookup(tmpl, maxMatches, filter);
    }

    /**
     * @param tmpl
     * @param filter
     * @return
     */
    public ServiceItem lookup(ServiceTemplate tmpl, ServiceItemFilter filter) {
        return this.lookup(tmpl, filter, false);
    }

    public ServiceItem lookup(ServiceTemplate tmpl, ServiceItemFilter filter, boolean lookupLocal) {
        if (lookupLocal || localOnly || lookupInCache) {
            return jiniAndLocal.lookup(tmpl, filter);
        }
        return sdm.lookup(tmpl, filter);
    }

    /**
     * @param tmpl
     * @param filter
     * @param waitDur
     * @return
     * @throws java.lang.InterruptedException
     * @throws java.rmi.RemoteException
     */
    public ServiceItem lookup(ServiceTemplate tmpl, ServiceItemFilter filter, long waitDur)
            throws InterruptedException, RemoteException {
        return this.lookup(tmpl, filter, waitDur, false);
    }

    public ServiceItem lookup(ServiceTemplate tmpl, ServiceItemFilter filter, long waitDur, boolean lookupLocal)
            throws InterruptedException, RemoteException {
        if (lookupLocal || localOnly || lookupInCache) {
            return jiniAndLocal.lookup(tmpl, filter, waitDur);
        }
        return this.sdm.lookup(tmpl, filter, waitDur);
    }

    public Transaction createTransaction(long maxDuration) throws LeaseDeniedException, RemoteException,
            InterruptedException {
        if (maxDuration == Long.MAX_VALUE) {
            throw new LeaseDeniedException("Please pick a more appropriate maximum transaction duration...");
        }
        ServiceItem tms;
        ServiceTemplate tmpl = new ServiceTemplate(null, new Class[] { TransactionManager.class }, null);
        ServiceItemFilter filter = null;
        if (localOnly) {
            tms = this.jiniAndLocal.lookup(tmpl, filter);
        } else {
            tms = this.transactionManagerCache.lookup(null);
            if (tms == null) {
                tms = this.lookup(tmpl, filter, Lease.FOREVER);
            }
        }
        TransactionManager tm = (TransactionManager) tms.service;
        Transaction.Created tc = TransactionFactory.create(tm, renewDuration);
        this.leaseRenewalManager.renewFor(tc.lease, maxDuration, this.renewDuration, null);
        return tc.transaction;
    }

    /**
     * @param lease
     * @throws net.jini.core.lease.UnknownLeaseException
     * @throws java.rmi.RemoteException
     */
    public void cancel(Lease lease) throws UnknownLeaseException, RemoteException {
        leaseRenewalManager.cancel(lease);
    }

    /**
     * 
     */
    public void clear() {
        leaseRenewalManager.clear();
    }

    /**
     * @param lease
     * @return
     * @throws net.jini.core.lease.UnknownLeaseException
     */
    public long getExpiration(Lease lease) throws UnknownLeaseException {
        return leaseRenewalManager.getExpiration(lease);
    }

    /**
     * @param lease
     * @throws net.jini.core.lease.UnknownLeaseException
     */
    public void remove(Lease lease) throws UnknownLeaseException {
        leaseRenewalManager.remove(lease);
    }

    /**
     * @param lease
     * @param desiredDuration
     * @param renewDuration
     * @param listener
     */
    public void renewFor(Lease lease, long desiredDuration, long renewDuration, LeaseListener listener) {
        leaseRenewalManager.renewFor(lease, desiredDuration, renewDuration, listener);
    }

    /**
     * @param lease
     * @param desiredDuration
     * @param listener
     */
    public void renewFor(Lease lease, long desiredDuration, LeaseListener listener) {
        leaseRenewalManager.renewFor(lease, desiredDuration, listener);
    }

    /**
     * @param lease
     * @param desiredExpiration
     * @param renewDuration
     * @param listener
     */
    public void renewUntil(Lease lease, long desiredExpiration, long renewDuration, LeaseListener listener) {
        leaseRenewalManager.renewUntil(lease, desiredExpiration, renewDuration, listener);
    }

    /**
     * @param lease
     * @param desiredExpiration
     * @param listener
     */
    public void renewUntil(Lease lease, long desiredExpiration, LeaseListener listener) {
        leaseRenewalManager.renewUntil(lease, desiredExpiration, listener);
    }

    /**
     * @param lease
     * @param expiration
     * @throws net.jini.core.lease.UnknownLeaseException
     */
    public void setExpiration(Lease lease, long expiration) throws UnknownLeaseException {
        leaseRenewalManager.setExpiration(lease, expiration);
    }

    public void addLocalService(ServiceItem service) {
        this.jiniAndLocal.addLocalService(service);
    }

    public void addGroups(String[] groups) throws IOException {
        this.getLookupDiscoveryManager().addGroups(groups);
        logger.info("Added discovery groups: " + Arrays.asList(groups) + " all groups: "
            + Arrays.asList(this.getLookupDiscoveryManager().getGroups()));
    }

    public static boolean isLocalOnly() {
        return localOnly;
    }

    public static void setLocalOnly(boolean localOnly) {
        JiniManager.localOnly = localOnly;
    }
}
