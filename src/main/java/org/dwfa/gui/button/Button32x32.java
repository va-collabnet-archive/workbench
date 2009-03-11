package org.dwfa.gui.button;

import javax.swing.Action;
import javax.swing.Icon;

public class Button32x32 extends Button24x24 {

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   protected int getPixels() {
      return 44;
   }

   public Button32x32() {
      super();
   }

   public Button32x32(Action arg0) {
      super(arg0);
   }

   public Button32x32(Icon arg0) {
      super(arg0);
   }

   public Button32x32(String arg0, Icon arg1) {
      super(arg0, arg1);
   }

   public Button32x32(String arg0) {
      super(arg0);
   }

}
