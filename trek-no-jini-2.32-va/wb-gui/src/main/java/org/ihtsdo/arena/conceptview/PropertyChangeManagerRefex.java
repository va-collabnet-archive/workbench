/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ihtsdo.arena.conceptview;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.dwfa.ace.api.I_GetConceptData;
import org.ihtsdo.tk.api.TypedComponentAnalogBI;
import org.ihtsdo.tk.api.refex.RefexAnalogBI;

/**
 *
 * @author kec
 */
public abstract class PropertyChangeManagerRefex<T extends RefexAnalogBI> 
      implements PropertyChangeListener {
      
      private T component;
      
      public PropertyChangeManagerRefex(T component) {
         super();
         this.setComponent(component);
      }
      
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
          if(I_GetConceptData.class.isAssignableFrom(evt.getNewValue().getClass())){
              I_GetConceptData newValue = (I_GetConceptData) evt.getNewValue();
              changeProperty(newValue);
          }else{
              String newValue = (String) evt.getNewValue();
              changeProperty(newValue);
          }
         
      }
      
      protected abstract void changeProperty(I_GetConceptData newValue);
      protected abstract void changeProperty(String newValue);
      
      protected final void setComponent(T component) {
         this.component = component;
      }
      
      protected T getComponent() {
         return component;
      }
}
