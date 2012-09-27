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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeListenerProxy;

/**
 * A JavaBeans based interface for managing properties.
 * 
 * @author kec
 * 
 * @model
 */
public interface I_ManageProperties {
    /**
     * Add a PropertyChangeListener to the listener list.
     * The listener is registered for all properties.
     * 
     * @param listener The PropertyChangeListener to be added
     */
    public void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Remove a PropertyChangeListener from the listener list.
     * This removes a PropertyChangeListener that was registered
     * for all properties.
     * 
     * @param listener The PropertyChangeListener to be removed
     */
    public void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * Returns an array of all the listeners that were added to the
     * PropertyChangeSupport object with addPropertyChangeListener().
     * <p>
     * If some listeners have been added with a named property, then the
     * returned array will be a mixture of PropertyChangeListeners and
     * <code>PropertyChangeListenerProxy</code>s. If the calling method is
     * interested in distinguishing the listeners then it must test each element
     * to see if it's a <code>PropertyChangeListenerProxy</code>, perform the
     * cast, and examine the parameter.
     * 
     * <pre>
     * PropertyChangeListener[] listeners = bean.getPropertyChangeListeners();
     * for (int i = 0; i &lt; listeners.length; i++) {
     *     if (listeners[i] instanceof PropertyChangeListenerProxy) {
     *         PropertyChangeListenerProxy proxy = (PropertyChangeListenerProxy) listeners[i];
     *         if (proxy.getPropertyName().equals(&quot;foo&quot;)) {
     *             // proxy is a PropertyChangeListener which was associated
     *             // with the property named &quot;foo&quot;
     *         }
     *     }
     * }
     *</pre>
     * 
     * @see PropertyChangeListenerProxy
     * @return all of the <code>PropertyChangeListeners</code> added or an
     *         empty array if no listeners have been added
     * @since 1.4
     */
    public PropertyChangeListener[] getPropertyChangeListeners();

    /**
     * Add a PropertyChangeListener for a specific property. The listener
     * will be invoked only when a call on firePropertyChange names that
     * specific property.
     * 
     * @param propertyName The name of the property to listen on.
     * @param listener The PropertyChangeListener to be added
     */

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

    /**
     * Remove a PropertyChangeListener for a specific property.
     * 
     * @param propertyName The name of the property that was listened on.
     * @param listener The PropertyChangeListener to be removed
     */

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);

    /**
     * Returns an array of all the listeners which have been associated
     * with the named property.
     * 
     * @return all of the <code>PropertyChangeListeners</code> associated with
     *         the named property or an empty array if no listeners have
     *         been added
     */
    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName);

}
