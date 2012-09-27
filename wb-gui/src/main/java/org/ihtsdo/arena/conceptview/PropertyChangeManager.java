/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ihtsdo.arena.conceptview;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.dwfa.ace.api.I_GetConceptData;
import org.ihtsdo.tk.api.TypedComponentAnalogBI;

/**
 *
 * @author kec
 */
public abstract class PropertyChangeManager<T extends TypedComponentAnalogBI> 
      implements PropertyChangeListener {
      
      private T component;
      
      public PropertyChangeManager(T component) {
         super();
         this.setComponent(component);
      }
      
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
         I_GetConceptData newValue = (I_GetConceptData) evt.getNewValue();
         changeProperty(newValue);
      }
      
      protected abstract void changeProperty(I_GetConceptData newValue);
      
      protected final void setComponent(T component) {
         this.component = component;
      }
      
      protected T getComponent() {
         return component;
      }
}
