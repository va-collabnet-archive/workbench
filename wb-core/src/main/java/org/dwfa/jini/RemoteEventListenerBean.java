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

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.logging.Logger;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import net.jini.export.Exporter;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.tcp.TcpServerEndpoint;

/**
 * @author kec
 * 
 */
public class RemoteEventListenerBean implements RemoteEventListener {

    private Configuration config;

    private RemoteEventListener proxy;

    private Collection<RemoteEventListener> listeners = new HashSet<RemoteEventListener>();

    /**
     * @throws ConfigurationException
     * @throws PrivilegedActionException
     * @throws RemoteException
     * @throws LoginException
     * 
     */
    public RemoteEventListenerBean(Configuration config) throws LoginException, RemoteException,
            PrivilegedActionException, ConfigurationException {
        this.config = config;
        this.init();
    }

    public void addRemoteEventListener(RemoteEventListener remEvtLsnr) {
        this.listeners.add(remEvtLsnr);
    }

    public void removeRemoteEventListener(RemoteEventListener remEvtLsnr) {
        this.listeners.remove(remEvtLsnr);
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
        proxy = RemoteEventListenerProxy.create((RemoteEventListener) backend);
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

    /**
     * @see net.jini.core.event.RemoteEventListener#notify(net.jini.core.event.RemoteEvent)
     */
    public void notify(RemoteEvent theEvent) throws UnknownEventException, RemoteException {
        Iterator<RemoteEventListener> listenerItr = this.listeners.iterator();
        while (listenerItr.hasNext()) {
            RemoteEventListener remEvtLsnr = listenerItr.next();
            remEvtLsnr.notify(theEvent);
        }
    }

    /**
     * @return Returns the proxy.
     */
    public RemoteEventListener getProxy() {
        return proxy;
    }
}
