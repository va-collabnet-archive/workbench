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
 * Created on Feb 25, 2005
 */
package org.dwfa.jini;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import net.jini.core.lookup.ServiceItem;
import net.jini.lookup.ServiceDiscoveryEvent;
import net.jini.lookup.ServiceDiscoveryListener;

/**
 * @author kec
 * 
 */
public class ServiceListModel implements ServiceDiscoveryListener, ListModel {

    private List<ServiceItem> services = Collections.synchronizedList(new ArrayList<ServiceItem>());

    private Set<ListDataListener> listeners = Collections.synchronizedSet(new HashSet<ListDataListener>());

    /**
	 *  
	 */
    public ServiceListModel() {
        super();
    }

    /**
     * @see net.jini.lookup.ServiceDiscoveryListener#serviceAdded(net.jini.lookup.ServiceDiscoveryEvent)
     */
    public void serviceAdded(ServiceDiscoveryEvent event) {
        SwingUtilities.invokeLater(new RunServiceAdded(event));
    }

    /**
     * @param event
     */
    private class RunServiceAdded implements Runnable {
        ServiceDiscoveryEvent event;

        /**
         * @param event
         */
        public RunServiceAdded(ServiceDiscoveryEvent event) {
            super();
            this.event = event;
        }

        public void run() {
            ServiceItem service = event.getPostEventServiceItem();
            ListDataEvent lde = null;
            synchronized (ServiceListModel.this.services) {
                int index = ServiceListModel.this.services.size();
                ServiceListModel.this.services.add(index, service);
                lde = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, index, index);
            }
            synchronized (ServiceListModel.this.listeners) {
                for (Iterator<ListDataListener> listenerItr = ServiceListModel.this.listeners.iterator(); listenerItr.hasNext();) {
                    ListDataListener ldr = listenerItr.next();
                    ldr.intervalAdded(lde);
                }
            }
        }
    }

    /**
     * @see net.jini.lookup.ServiceDiscoveryListener#serviceRemoved(net.jini.lookup.ServiceDiscoveryEvent)
     */
    public void serviceRemoved(ServiceDiscoveryEvent event) {
        SwingUtilities.invokeLater(new RunServiceRemoved(event));
    }

    /**
     * @param event
     */
    private class RunServiceRemoved implements Runnable {
        ServiceDiscoveryEvent event;

        /**
         * @param event
         */
        public RunServiceRemoved(ServiceDiscoveryEvent event) {
            super();
            this.event = event;
        }

        public void run() {
            ServiceItem service = event.getPreEventServiceItem();
            ListDataEvent lde = null;
            synchronized (ServiceListModel.this.services) {
                int index = ServiceListModel.this.services.indexOf(service);
                ServiceListModel.this.services.remove(service);
                lde = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, index, index);
            }
            synchronized (ServiceListModel.this.listeners) {
                for (Iterator<ListDataListener> listenerItr = ServiceListModel.this.listeners.iterator(); listenerItr.hasNext();) {
                    ListDataListener ldr = listenerItr.next();
                    ldr.intervalRemoved(lde);
                }
            }
        }
    }

    /**
     * @see net.jini.lookup.ServiceDiscoveryListener#serviceChanged(net.jini.lookup.ServiceDiscoveryEvent)
     */
    public void serviceChanged(ServiceDiscoveryEvent event) {
        SwingUtilities.invokeLater(new RunServiceChanged(event));
    }

    /**
     * @param event
     */
    private class RunServiceChanged implements Runnable {
        ServiceDiscoveryEvent event;

        /**
         * @param event
         */
        public RunServiceChanged(ServiceDiscoveryEvent event) {
            super();
            this.event = event;
        }

        public void run() {
            ServiceItem preService = event.getPreEventServiceItem();
            ServiceItem postService = event.getPostEventServiceItem();
            ListDataEvent lde = null;
            synchronized (ServiceListModel.this.services) {
                int index = ServiceListModel.this.services.indexOf(preService);
                if (index != -1) {
                    ServiceListModel.this.services.set(index, postService);
                    lde = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, index, index);
                }
            }
            synchronized (ServiceListModel.this.listeners) {
                for (Iterator<ListDataListener> listenerItr = ServiceListModel.this.listeners.iterator(); listenerItr.hasNext();) {
                    ListDataListener ldr = listenerItr.next();
                    ldr.contentsChanged(lde);
                }
            }
        }
    }

    /**
     * @see javax.swing.ListModel#getSize()
     */
    public int getSize() {
        return this.services.size();
    }

    /**
     * @see javax.swing.ListModel#getElementAt(int)
     */
    public Object getElementAt(int index) {
        return this.services.get(index);
    }

    /**
     * @see javax.swing.ListModel#addListDataListener(javax.swing.event.ListDataListener)
     */
    public void addListDataListener(ListDataListener l) {
        synchronized (this.listeners) {
            this.listeners.add(l);
        }

    }

    /**
     * @see javax.swing.ListModel#removeListDataListener(javax.swing.event.ListDataListener)
     */
    public void removeListDataListener(ListDataListener l) {
        synchronized (this.listeners) {
            this.listeners.remove(l);
        }
    }

}
