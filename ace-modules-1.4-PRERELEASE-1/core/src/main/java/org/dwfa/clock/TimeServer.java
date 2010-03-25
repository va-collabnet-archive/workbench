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
package org.dwfa.clock;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.rmi.server.ExportException;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.config.ConfigurationProvider;
import net.jini.config.NoSuchEntryException;
import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceID;
import net.jini.discovery.DiscoveryManagement;
import net.jini.discovery.LookupDiscovery;
import net.jini.export.Exporter;
import net.jini.export.ProxyAccessor;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.tcp.TcpServerEndpoint;
import net.jini.lookup.JoinManager;
import net.jini.security.TrustVerifier;
import net.jini.security.proxytrust.ServerProxyTrust;

import com.sun.jini.start.LifeCycle;

public class TimeServer implements I_KeepIncrementalTime, ServerProxyTrust, ProxyAccessor {

    private static Logger logger = Logger.getLogger(TimeServer.class.getName());

    I_KeepIncrementalTime timer;

    /**
     * Creates a new service ID.
     * 
     * @throws IOException
     */
    protected static ServiceID createServiceID(File directory) throws IOException {
        UUID uuid = UUID.randomUUID();
        ServiceID sid = new ServiceID(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
        File serviceIdFile = new File(directory, "ServiceID.oos");
        FileOutputStream fos = new FileOutputStream(serviceIdFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(sid);
        oos.flush();
        oos.close();
        return sid;
    }

    /**
     * Cache of our <code>LifeCycle</code> object TODO implement the lifeCycle
     * destroy methods. See TxnManagerImpl for an example.
     */
    protected LifeCycle lifeCycle = null;

    protected JoinManager joinManager;

    protected final Configuration config;

    protected ServiceID serviceId;

    protected File directory;

    public TimeServer(String[] args, LifeCycle lc) throws Exception {
        super();
        logger.info("\n*******************\n\n" + "Starting " + this.getClass().getSimpleName() + " with config file: "
            + Arrays.asList(args) + "\n\n******************\n");
        this.config = ConfigurationProvider.getInstance(args, getClass().getClassLoader());
        this.lifeCycle = lc;

        this.directory = (File) this.config.getEntry(this.getClass().getName(), "directory", File.class);

        this.timer = (I_KeepIncrementalTime) this.config.getEntry(this.getClass().getName(), "timer",
            I_KeepIncrementalTime.class);
        this.directory.mkdirs();
        initFromDirectory();
        this.init();
    }

    protected void initFromDirectory() throws IOException, ClassNotFoundException {
        File serviceIdFile = new File(this.directory, "ServiceID.oos");
        if (serviceIdFile.exists()) {
            FileInputStream fis = new FileInputStream(serviceIdFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ObjectInputStream ois = new ObjectInputStream(bis);
            this.serviceId = (ServiceID) ois.readObject();
        } else {
            this.getServiceID();
        }

    }

    /**
     * Returns the service ID for this server.
     * 
     * @throws IOException
     */
    protected ServiceID getServiceID() throws IOException {
        if (this.serviceId == null) {
            this.serviceId = createServiceID(this.directory);
        }
        return this.serviceId;
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
        LoginContext loginContext = (LoginContext) config.getEntry(this.getClass().getName(), "loginContext",
            LoginContext.class, null);
        if (loginContext == null) {
            logger.info("initAsSubject: " + this.getClass().getName() + " " + this.getServiceID()
                + " with no login context.");
            initAsSubject();
        } else {
            loginContext.login();
            StringBuffer message = new StringBuffer();
            message.append("initAsSubject: " + this.getClass().getName() + " " + this.getServiceID() + " with subject");
            if (logger.isLoggable(Level.FINE)) {
                message.append(loginContext.getSubject());
            }
            message.append(".");

            logger.info(message.toString());
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
     */
    protected void initAsSubject() throws Exception {
        /* Export the server */
        Exporter exporter = getExporter();

        /* Create the smart proxy */
        Object smartProxy = export(exporter);

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
        /*
         * Boolean publishLocalProxy = (Boolean) this.config.getEntry(this
         * .getClass().getName(), "publishLocalProxy", Boolean.class,
         * new Boolean(false));
         * if (publishLocalProxy.booleanValue()) {
         * ArrayList<Entry> entryList = new ArrayList<Entry>(Arrays
         * .asList(entries));
         * ListIterator<Entry> itr = entryList.listIterator();
         * while (itr.hasNext()) {
         * Entry entry = itr.next();
         * if (Name.class.isAssignableFrom(entry.getClass())) {
         * Name oldName = (Name) entry;
         * Name newName = new Name(oldName.name + " (local)");
         * itr.remove();
         * itr.add(newName);
         * break;
         * }
         * 
         * }
         * 
         * ServiceItem serviceItem = new ServiceItem(this.getServiceID(),
         * this, (Entry[]) entryList.toArray(new Entry[entryList
         * .size()]));
         * LookupJiniAndLocal.addToLocalServices(serviceItem);
         * 
         * }
         */
    }

    /** The server proxy, for use by getProxyVerifier */
    protected I_KeepIncrementalTime serverProxy;

    /**
     * @param exporter
     * @throws ExportException
     */
    protected Object export(Exporter exporter) throws ExportException {
        serverProxy = (I_KeepIncrementalTime) exporter.export(this);
        return TimerProxy.create(serverProxy);
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

    public long getTime() throws RemoteException {
        return timer.getTime();
    }

    public void increment() throws RemoteException {
        timer.increment();
    }

    public void reset() throws RemoteException {
        timer.reset();
    }

    public TrustVerifier getProxyVerifier() throws RemoteException {
        return new TimerProxy.Verifier(serverProxy);
    }

    public Object getProxy() {
        return serverProxy;
    }

}
