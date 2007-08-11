package org.dwfa.gui.toggle;

import java.awt.Dimension;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JToggleButton;

public class Toggle24x24  extends JToggleButton {

   
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public Toggle24x24() {
      setSize(this, getPixels());
   }

   protected int getPixels() {
      return 28;
   }
   private static void setSize(JToggleButton toggle, int pixels) {
      Dimension size = new Dimension(pixels, pixels);
      toggle.setMaximumSize(size);
      toggle.setSize(size);
      toggle.setMinimumSize(size);
   }

   public Toggle24x24(Icon arg0) {
      super(arg0);
      setSize(this, getPixels());
   }

   public Toggle24x24(String arg0) {
      super(arg0);
      setSize(this, getPixels());
   }

   public Toggle24x24(Action arg0) {
      super(arg0);
      setSize(this, getPixels());
   }

   public Toggle24x24(Icon arg0, boolean arg1) {
      super(arg0, arg1);
      setSize(this, getPixels());
   }

   public Toggle24x24(String arg0, boolean arg1) {
      super(arg0, arg1);
      setSize(this, getPixels());
   }

   public Toggle24x24(String arg0, Icon arg1) {
      super(arg0, arg1);
      setSize(this, getPixels());
   }

   public Toggle24x24(String arg0, Icon arg1, boolean arg2) {
      super(arg0, arg1, arg2);
      setSize(this, getPixels());
   }

   
}
