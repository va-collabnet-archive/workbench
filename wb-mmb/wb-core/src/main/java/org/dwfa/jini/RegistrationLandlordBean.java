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
 * Created on Mar 7, 2005
 */
package org.dwfa.jini;

import java.rmi.MarshalledObject;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.logging.Logger;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.core.event.EventRegistration;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.lease.Lease;
import net.jini.core.lease.UnknownLeaseException;
import net.jini.export.Exporter;
import net.jini.id.Uuid;
import net.jini.id.UuidFactory;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.tcp.TcpServerEndpoint;

import com.sun.jini.landlord.Landlord;
import com.sun.jini.landlord.LeaseFactory;

/**
 * @author kec
 * 
 */
public class RegistrationLandlordBean implements Landlord {

    // A simple leasing policy...10-minute leases.
    protected static final int DEFAULT_MAX_LEASE = 1000 * 60 * 10;

    protected static long cookiecount = 0;

    protected int maxLease = DEFAULT_MAX_LEASE;

    protected List<Registration> regs;

    private Configuration config;

    private Landlord proxy;

    /**
     * @return Returns the proxy.
     */
    public Landlord getProxy() {
        return proxy;
    }

    // A factory for making landlord leases
    protected LeaseFactory factory;

    public RegistrationLandlordBean(Configuration config) throws LoginException, RemoteException,
            PrivilegedActionException, ConfigurationException {
        this.config = config;
        this.regs = Collections.synchronizedList(new ArrayList<Registration>());
        this.init();
        this.factory = new LeaseFactory(this.getProxy(), UuidFactory.generate());
    }

    /** Component name for service starter configuration entries */
    static final String START_PACKAGE = "com.sun.jini.start";

    private static/* final */Logger logger = null;
    static {
        try {
            logger = Logger.getLogger(START_PACKAGE + ".service.starter", START_PACKAGE + ".resources.service");
        } catch (Exception e) {
            logger = Logger.getLogger(START_PACKAGE + ".service.starter");
            if (e instanceof MissingResourceException) {
                logger.info("Could not load logger's ResourceBundle: " + e);
            } else if (e instanceof IllegalArgumentException) {
                logger.info("Logger exists and uses another resource bundle: " + e);
            }
            logger.info("Defaulting to existing logger");
        }
    }

    @SuppressWarnings("unchecked")
    private void init() throws LoginException, PrivilegedActionException, RemoteException, ConfigurationException {
        LoginContext loginContext = (LoginContext) config.getEntry(this.getClass().getName(), "loginContext",
            LoginContext.class, null);
        if (loginContext == null) {
            initAsSubject();
        } else {
            loginContext.login();
            Subject.doAsPrivileged(loginContext.getSubject(), new PrivilegedExceptionAction() {
                public Object run() throws Exception {
                    initAsSubject();
                    return null;
                }
            }, null);
        }

    }

    /**
     * Initializes the server, assuming that the appropriate subject is in
     * effect.
     * 
     * @throws ConfigurationException
     * @throws RemoteException
     */
    protected void initAsSubject() throws RemoteException, ConfigurationException {
        /* Export the server */
        Exporter exporter = getExporter();
        Remote backend = exporter.export(this);

        /* Create the smart proxy */
        proxy = LandlordProxy.create((Landlord) backend);
    }

    /**
     * Returns the exporter for exporting the server.
     * 
     * @throws ConfigurationException
     *             if a problem occurs getting the exporter from the
     *             configuration
     * @throws RemoteException
     *             if a remote communication problem occurs
     */
    protected Exporter getExporter() throws ConfigurationException, RemoteException {
        return (Exporter) config.getEntry(this.getClass().getName(), "exporter", Exporter.class, new BasicJeriExporter(
            TcpServerEndpoint.getInstance(0), new BasicILFactory()));
    }

    public List<Registration> getRegs() {
        return regs;
    }

    // Change the maximum lease time from the default
    public void setMaxLease(int maxLease) {
        this.maxLease = maxLease;
    }

    // Apply the policy to a requested duration
    // to get an actual expiration time.
    public long getExpiration(long request) {
        if (request > maxLease || request == Lease.ANY)
            return System.currentTimeMillis() + maxLease;
        else
            return System.currentTimeMillis() + request;
    }

    // This adds a registration to the list , creates a lease and
    // returns a event registration that can be sent back to the
    // event requester.

    public EventRegistration addEventReg(Uuid cookie, long eventType, MarshalledObject data,
            RemoteEventListener listener, Object source, long duration) {

        // Create the lease and registration, and add the
        // registration to list
        long expiration = getExpiration(duration);

        // New registration
        Registration reg = new Registration(cookie, listener, data, expiration);
        // New Lease
        Lease lease = factory.newLease(cookie, expiration);

        // Add the resource...
        regs.add(reg);

        // Create and return an event registration
        EventRegistration evtreg = new EventRegistration(eventType, source, lease, 0);

        return evtreg;
    }

    // ===== The LandLord Interface provided to Clients ============
    // Cancel the lease represented by 'cookie'
    public void cancel(Uuid cookie) throws UnknownLeaseException {
        synchronized (regs) {
            Iterator<Registration> regItr = regs.iterator();
            while (regItr.hasNext()) {
                Registration reg = regItr.next();
                if (reg.cookie.equals(cookie)) {
                    reg.cancelled();
                    regItr.remove();
                    return;
                }
            }
        }
        throw new UnknownLeaseException(cookie.toString());
    }

    // Cancel a set of leases
    public Map<Uuid, UnknownLeaseException> cancelAll(Uuid[] cookies) {
        Map<Uuid, UnknownLeaseException> exceptionMap = null;

        for (int i = 0; i < cookies.length; i++) {
            try {
                cancel(cookies[i]);
            } catch (UnknownLeaseException ex) {
                if (exceptionMap == null) {
                    exceptionMap = new HashMap<Uuid, UnknownLeaseException>();
                }
                exceptionMap.put(cookies[i], ex);
            }
        }

        return exceptionMap;
    }

    // Renew the lease specified by 'cookie'
    public long renew(Uuid cookie, long extension) throws UnknownLeaseException {
        synchronized (regs) {
            Iterator<Registration> regItr = regs.iterator();
            while (regItr.hasNext()) {
                Registration reg = regItr.next();
                if (reg.cookie.equals(cookie)) {
                    long expiration = getExpiration(extension);
                    reg.setExpiration(expiration);
                    return expiration - System.currentTimeMillis();
                }
            }
        }
        throw new UnknownLeaseException(cookie.toString());
    }

    // Renew a set of leases.
    public Landlord.RenewResults renewAll(Uuid[] cookies, long[] extensions) {
        long[] granted = new long[cookies.length];
        Exception[] denied = null;

        for (int i = 0; i < cookies.length; i++) {
            try {
                granted[i] = renew(cookies[i], extensions[i]);
            } catch (Exception ex) {
                if (denied == null) {
                    denied = new Exception[cookies.length + 1];
                }
                denied[i + 1] = ex;
            }
        }

        Landlord.RenewResults results = new Landlord.RenewResults(granted, denied);
        return results;
    }

}
