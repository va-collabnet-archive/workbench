/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.tk.api;

//~--- JDK imports ------------------------------------------------------------
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The listener interface for receiving termChange events. The class that is
 * interested in processing a termChange event implements this interface, and
 * the object created with that class is registered with a component using the
 * component's
 * <code>addTermChangeListener<code> method. When
 * the termChange event occurs, that object's appropriate
 * method is invoked.
 *
 */
public abstract class TermChangeListener {

    private static AtomicInteger listenerIdSequence = new AtomicInteger();
    //~--- fields --------------------------------------------------------------
    private int listenerId = listenerIdSequence.incrementAndGet();

    //~--- methods -------------------------------------------------------------
    /**
     * Allows the implemented method to do process the sets of changed nids when
     * a term change event occurs.
     *
     * @param sequence a long representing the change in the database associated
     * with the changed nids
     * @param sourcesOfChangedRels the nids associated with the sources of
     * changed relationships
     * @param targetsOfChangedRels the nids associated with the targets of
     * changed relationships
     * @param referencedComponentsOfChangedRefexs the nids associated with the
     * referenced components of changed refexs
     * @param changedComponents the nids associated with the changed components
     * @param changedComponentAlerts the nids associated with the changed
     * component alerts
     * @param changedComponentTypes the the nids associated with the changed
     * component types
     */
    public abstract void changeNotify(long sequence,
            Set<Integer> sourcesOfChangedRels,
            Set<Integer> targetsOfChangedRels, 
            Set<Integer> referencedComponentsOfChangedRefexs, 
            Set<Integer> changedComponents, 
            Set<Integer> changedComponentAlerts, 
            Set<Integer> changedComponentTypes, 
            boolean fromClassification);

    //~--- get methods ---------------------------------------------------------
    /**
     * Gets the id of the associated listener.
     *
     * @return the listener id
     */
    public int getListenerId() {
        return listenerId;
    }
}
