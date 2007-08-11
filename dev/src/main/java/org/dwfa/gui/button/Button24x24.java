package org.dwfa.gui.button;

import java.awt.Dimension;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;

public class Button24x24 extends JButton {

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   protected int getPixels() {
      return 28;
   }
   private static void setSize(JButton button, int pixels) {
      Dimension size = new Dimension(pixels, pixels);
      button.setMaximumSize(size);
      button.setSize(size);
      button.setMinimumSize(size);
   }
   public Button24x24() {
      super();
      setSize(this, getPixels());
   }
   public Button24x24(Action arg0) {
      super(arg0);
      setSize(this, getPixels());
   }
   public Button24x24(Icon arg0) {
      super(arg0);
      setSize(this, getPixels());
   }
   public Button24x24(String arg0, Icon arg1) {
      super(arg0, arg1);
      setSize(this, getPixels());
   }
   public Button24x24(String arg0) {
      super(arg0);
      setSize(this, getPixels());
   }

}
