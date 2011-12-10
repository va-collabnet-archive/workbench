/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.db.bdb;

import java.beans.*;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.TerminologyStoreDI.PC_EVENT;

/**
 *
 * @author AKF
 */
public class GlobalPropertyChange {
    
    private static class WeakRefListener implements PropertyChangeListener {
        WeakReference<PropertyChangeListener> wr;
        int hash; 
        String objStr;

        public WeakRefListener(PropertyChangeListener l) {
            this.wr = new WeakReference<PropertyChangeListener>(l);
            objStr = l.toString();
            hash = 7;
            hash = 13 * hash + (this.wr != null ? this.wr.hashCode() : 0);
        }

        @Override
        public void propertyChange(PropertyChangeEvent pce) {
            PropertyChangeListener pcl = wr.get();
            if (pcl != null) {
                pcl.propertyChange(pce);
            } else {
                listenerToRemove.add(this);
            }
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            return this.objStr.equals(obj.toString());
        }
        
        
    }
    
    private static class WeakRefVetoListener implements VetoableChangeListener {
        WeakReference<VetoableChangeListener> wr;
        int hash; 
        String objStr;

        public WeakRefVetoListener(VetoableChangeListener l) {
            this.wr = new WeakReference<VetoableChangeListener>(l);
            this.objStr = l.toString();
            hash = 7;
            hash = 13 * hash + (this.wr != null ? this.wr.hashCode() : 0);
        }


        @Override
        public void vetoableChange(PropertyChangeEvent pce) throws PropertyVetoException {
            VetoableChangeListener pcl = wr.get();
            if (pcl != null) {
                pcl.vetoableChange(pce);
            } else {
                vetoListenerToRemove.add(this);
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            return this.objStr.equals(obj.toString());
        }

        @Override
        public int hashCode() {
            return hash;
        }
        
        
    }
    
    private static GlobalPropertyChange s = new GlobalPropertyChange();

    private GlobalPropertyChange() {
        gPcs = new PropertyChangeSupport(this);
        gVcs = new VetoableChangeSupport(this);
    }
    private static PropertyChangeSupport gPcs;
    private static VetoableChangeSupport gVcs;
    private static List<PropertyChangeListener> listenerToRemove = new ArrayList<PropertyChangeListener>();
    private static List<VetoableChangeListener> vetoListenerToRemove = new ArrayList<VetoableChangeListener>();
    
    public static void addPropertyChangeListener(TerminologyStoreDI.PC_EVENT eventType, PropertyChangeListener listener) {
        gPcs.addPropertyChangeListener(eventType.toString(), new WeakRefListener(listener));
    }
    
    public static void removePropertyChangeListener(PropertyChangeListener listener) {
        gPcs.removePropertyChangeListener(listener);
    }

    public static void addVetoableChangeListener(TerminologyStoreDI.PC_EVENT eventType, VetoableChangeListener listener) {
        gVcs.addVetoableChangeListener(eventType.toString(), new WeakRefVetoListener(listener));
    }
    
    public static void removeVetoableChangeListener(VetoableChangeListener listener) {
        gVcs.removeVetoableChangeListener(listener);
    }
    
    public static void firePropertyChange(PC_EVENT pce, Object oldValue, Object newValue){
        gPcs.firePropertyChange(pce.toString(), oldValue, oldValue);
        for (PropertyChangeListener l: listenerToRemove) {
            gPcs.removePropertyChangeListener(l);
        }
    }
    
    public static void fireVetoableChange(PC_EVENT pce, Object oldValue, Object newValue) throws PropertyVetoException{
        gVcs.fireVetoableChange(pce.toString(), oldValue, oldValue);
        for (VetoableChangeListener l: vetoListenerToRemove) {
            gVcs.removeVetoableChangeListener(l);
        }
   }

}
