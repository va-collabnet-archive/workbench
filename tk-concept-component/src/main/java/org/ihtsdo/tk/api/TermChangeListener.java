/**
 * Copyright (c) 2012 International Health Terminology Standards Development
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



package org.ihtsdo.tk.api;

//~--- JDK imports ------------------------------------------------------------

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

// TODO: Auto-generated Javadoc
/**
 * The listener interface for receiving termChange events.
 * The class that is interested in processing a termChange
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addTermChangeListener<code> method. When
 * the termChange event occurs, that object's appropriate
 * method is invoked.
 *
 * @author kec
 */
public abstract class TermChangeListener {
   
   /** The listener id sequence. */
   private static AtomicInteger listenerIdSequence = new AtomicInteger();

   //~--- fields --------------------------------------------------------------

   /** The listener id. */
   private int listenerId = listenerIdSequence.incrementAndGet();

   //~--- methods -------------------------------------------------------------

   /**
    * Change notify.
    *
    * @param sequence the sequence
    * @param sourcesOfChangedRels the sources of changed rels
    * @param targetsOfChangedRels the targets of changed rels
    * @param referencedComponentsOfChangedRefexs the referenced components of changed refexs
    * @param changedComponents the changed components
    * @param changedComponentAlerts the changed component alerts
    * @param changedComponentTypes the changed component types
    */
   public abstract void changeNotify(long sequence, 
                                     Set<Integer> sourcesOfChangedRels,
                                     Set<Integer> targetsOfChangedRels, 
                                     Set<Integer> referencedComponentsOfChangedRefexs, 
                                     Set<Integer> changedComponents,
                                     Set<Integer> changedComponentAlerts,
                                     Set<Integer> changedComponentTypes);

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the listener id.
    *
    * @return the listener id
    */
   public int getListenerId() {
      return listenerId;
   }
}
