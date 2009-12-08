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
package org.dwfa.bpa.process;

import java.beans.VetoableChangeListener;

/**
 * A java beans based interface for managing vetoable properties.
 * 
 * @author kec
 * 
 */
public interface I_ManageVetoableProperties {

    /**
     * Add a VetoableListener to the listener list.
     * The listener is registered for all properties.
     * 
     * @param listener The VetoableChangeListener to be added
     */

    public void addVetoableChangeListener(VetoableChangeListener listener);

    /**
     * Remove a VetoableChangeListener from the listener list.
     * This removes a VetoableChangeListener that was registered
     * for all properties.
     * 
     * @param listener The VetoableChangeListener to be removed
     */
    public void removeVetoableChangeListener(VetoableChangeListener listener);

    /**
     * Returns the list of VetoableChangeListeners. If named vetoable change
     * listeners
     * were added, then VetoableChangeListenerProxy wrappers will returned
     * <p>
     * 
     * @return List of VetoableChangeListeners and VetoableChangeListenerProxys
     *         if named property change listeners were added.
     */
    public VetoableChangeListener[] getVetoableChangeListeners();

    /**
     * Add a VetoableChangeListener for a specific property. The listener
     * will be invoked only when a call on fireVetoableChange names that
     * specific property.
     * 
     * @param propertyName The name of the property to listen on.
     * @param listener The VetoableChangeListener to be added
     */

    public void addVetoableChangeListener(String propertyName, VetoableChangeListener listener);

    /**
     * Remove a VetoableChangeListener for a specific property.
     * 
     * @param propertyName The name of the property that was listened on.
     * @param listener The VetoableChangeListener to be removed
     */

    public void removeVetoableChangeListener(String propertyName, VetoableChangeListener listener);

    /**
     * Returns an array of all the listeners which have been associated
     * with the named property.
     * 
     * @return all the <code>VetoableChangeListeners</code> associated with
     *         the named property or an empty array if no listeners have
     *         been added.
     */
    public VetoableChangeListener[] getVetoableChangeListeners(String propertyName);

}
