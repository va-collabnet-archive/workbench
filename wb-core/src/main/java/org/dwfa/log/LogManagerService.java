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
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.config.ConfigurationProvider;
import net.jini.config.NoSuchEntryException;
import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;
import net.jini.discovery.DiscoveryManagement;
import net.jini.discovery.LookupDiscovery;
import net.jini.export.Exporter;
import net.jini.id.Uuid;
import net.jini.id.UuidFactory;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.tcp.TcpServerEndpoint;
import net.jini.lookup.JoinManager;
import net.jini.lookup.entry.Name;
import net.jini.lookup.entry.ServiceInfo;
import net.jini.security.TrustVerifier;

import org.dwfa.jini.JiniManager;
import org.dwfa.jini.LookupJiniAndLocal;

import com.sun.jini.start.LifeCycle;

/**
 * @author kec
 * 
 */
public class LogManagerService implements I_ManageLogs {

    public static final String VERSION_STRING = "1.0";

    LogManagerAdaptor adaptor = new LogManagerAdaptor(LogManager.getLogManager());

    private static Logger logger = Logger.getLogger(LogManagerService.class.getName());

    /**
     * Cache of our <code>LifeCycle</code> object TODO implement the lifeCycle
     * destroy methods. See TxnManagerImpl for an example.
     */
    @SuppressWarnings("unused")
    private LifeCycle lifeCycle = null;

    private JoinManager joinManager;

    /** The configuration to use for configuring the server */
    protected final Configuration config;

    /** The server proxy, for use by getProxyVerifier */
    protected I_ManageLogs serverProxy;

    private ServiceID serviceId;

    /**
     * 
     */
    public LogManagerService(String[] args, LifeCycle lc) throws Exception {
        logger.info("\n*******************\n\n" + "Starting " + this.getClass().getSimpleName() + " with config file: "
            + Arrays.asList(args) + "\n\n******************\n");
        this.config = ConfigurationProvider.getInstance(args, getClass().getClassLoader());
        this.lifeCycle = lc;
        this.init();
    }

    /**
     * Initializes the server, including exporting it and storing its proxy in
     * the registry.
     * 
     * @throws Exception
     *             if a problem occurs
     */
    @SuppressWarnings("unchecked")
    protected void init() throws Exception {
        if (JiniManager.isLocalOnly()) {
            List<Entry> entryList = new ArrayList<Entry>();
            Entry[] entries = (Entry[]) this.config.getEntry(this.getClass().getName(), "entries", Entry[].class,
                new Entry[] {});
            entryList.addAll(Arrays.asList(entries));

            ServiceItem serviceItem = new ServiceItem(this.getServiceID(), this,
                (Entry[]) entryList.toArray(new Entry[entryList.size()]));
            LookupJiniAndLocal.addToLocalServices(serviceItem);

        } else {
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
    }

    /**
     * Initializes the server, assuming that the appropriate subject is in
     * effect.
     */
    protected void initAsSubject() throws Exception {
        /* Export the server */
        logger.info("initAsSubject: " + this.getClass().getName());
        Exporter exporter = getExporter();
        serverProxy = (I_ManageLogs) exporter.export(this);

        /* Create the smart proxy */
        LogManagerProxy smartProxy = LogManagerProxy.create(serverProxy);

        /* Get the discovery manager, for discovering lookup services */
        DiscoveryManagement discoveryManager;
        try {
            discoveryManager = (DiscoveryManagement) config.getEntry(this.getClass().getName(), "discoveryManager",
                DiscoveryManagement.class);
        } catch (NoSuchEntryException e) {
            logger.warning("No entry for discoveryManager in config file. " + e.toString());
            String[] groups = (String[]) config.getEntry(this.getClass().getName(), "groups", String[].class);
            discoveryManager = new LookupDiscovery(groups, config);
        }

        Entry[] entries = (Entry[]) this.config.getEntry(this.getClass().getName(), "entries", Entry[].class,
            new Entry[] {});

        /* Get the join manager, for joining lookup services */
        joinManager = new JoinManager(smartProxy, entries, getServiceID(), discoveryManager, null /* leaseMgr */,
            config);

        Entry[] moreEntries = new Entry[] { new ServiceInfo("Log manager service", "Informatics, Inc.",
            "Informatics, Inc.", VERSION_STRING, "Log manager", "no serial number") };
        joinManager.addAttributes(moreEntries);

        ArrayList<Entry> entryList = new ArrayList<Entry>(Arrays.asList(entries));
        entryList.addAll(Arrays.asList(moreEntries));
        ListIterator<Entry> itr = entryList.listIterator();
        while (itr.hasNext()) {
            Entry entry = itr.next();
            if (Name.class.isAssignableFrom(entry.getClass())) {
                Name oldName = (Name) entry;
                Name newName = new Name(oldName.name + " (local)");
                itr.remove();
                itr.add(newName);
                break;
            }

        }

        ServiceItem serviceItem = new ServiceItem(createServiceID(), this,
            (Entry[]) entryList.toArray(new Entry[entryList.size()]));
        LookupJiniAndLocal.addToLocalServices(serviceItem);
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

    /** Returns the service ID for this server. */
    protected ServiceID getServiceID() {
        if (this.serviceId == null) {
            createServiceID();
        }
        return this.serviceId;
    }

    /** Creates a new service ID. */
    protected static ServiceID createServiceID() {
        Uuid uuid = UuidFactory.generate();
        return new ServiceID(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
    }

    /**
     * Implement the ServerProxyTrust interface to provide a verifier for secure
     * smart proxies.
     */
    public TrustVerifier getProxyVerifier() {
        return new LogManagerProxy.Verifier(serverProxy);
    }

    /**
     * Returns a proxy object for this remote object.
     * 
     * @return our proxy
     */
    public Object getProxy() {
        return serverProxy;
    }

    /**
     * @see org.dwfa.log.I_ManageLogs#getLoggerNames()
     */
    public List<String> getLoggerNames() throws RemoteException {
        return adaptor.getLoggerNames();
    }

    /**
     * @see org.dwfa.log.I_ManageLogs#getLevel(java.lang.String)
     */
    public Object getLevel(String loggerName) throws RemoteException {
        return adaptor.getLevel(loggerName);
    }

    /**
     * @see org.dwfa.log.I_ManageLogs#isLoggable(java.lang.String,
     *      java.util.logging.Level)
     */
    public boolean isLoggable(String loggerName, Level level) throws RemoteException {
        return adaptor.isLoggable(loggerName, level);
    }

    /**
     * @throws RemoteException
     * @see org.dwfa.log.I_ManageLogs#addRemoteHandler(java.lang.String,
     *      org.dwfa.log.I_PublishLogRecord)
     */
    public boolean addRemoteHandler(String loggerName, I_PublishLogRecord remoteHandler) throws RemoteException {
        Logger logger = Logger.getLogger(loggerName);
        RemoteHandlerAdaptor adaptor = null;
        if (this.remoteHandlerAdaptors.containsKey(remoteHandler.getId())) {
            adaptor = this.remoteHandlerAdaptors.get(remoteHandler.getId());
        } else {
            adaptor = new RemoteHandlerAdaptor(remoteHandler, loggerName, this);
            this.remoteHandlerAdaptors.put(remoteHandler.getId(), adaptor);
        }
        logger.addHandler(adaptor);
        logger.info("Added remote handler to: " + loggerName + ".");
        return true;

    }

    Map<Uuid, RemoteHandlerAdaptor> remoteHandlerAdaptors = new HashMap<Uuid, RemoteHandlerAdaptor>();

    /**
     * @see org.dwfa.log.I_ManageLogs#removeRemoteHandler(java.lang.String,
     *      net.jini.id.Uuid)
     */
    public void removeRemoteHandler(String loggerName, Uuid id) {
        RemoteHandlerAdaptor adaptor = this.remoteHandlerAdaptors.get(id);
        Logger logger = Logger.getLogger(loggerName);
        logger.removeHandler(adaptor);
        this.remoteHandlerAdaptors.remove(id);
    }

}
