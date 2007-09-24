package org.dwfa.ace.log;

import java.awt.Component;
import java.util.logging.Level;

import org.dwfa.ace.api.AceEditor;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.util.LogWithAlerts;

public class AceLogWithAlerts extends LogWithAlerts {

   public AceLogWithAlerts(String logName) {
      super(logName);
   }

   public void nonModalAlertAndLogException(Component parent, Throwable ex) {
      nonModalAlertAndLogException(parent, Level.SEVERE, ex.getLocalizedMessage(), ex);
   }
   public void nonModalAlertAndLogException(Throwable ex) {
      nonModalAlertAndLogException(null, Level.SEVERE, ex.getLocalizedMessage(), ex);
   }
   public void nonModalAlertAndLogException(Level level, String message, Throwable ex) {
      nonModalAlertAndLogException(null, level, message, ex);
   }
   public void nonModalAlertAndLogException(Component parent, Level level, String message, Throwable ex) {
      getLogger().log(level, message, ex);
      //get front frame...
      if (level.intValue() <= Level.INFO.intValue()) {
         message = "<html>" + message;
      } else if (level.intValue() <= Level.WARNING.intValue()) {
         message = "<html><font color='red'>" + message;
      } else {
         message = "<html><font color='red'>" + message;
      } 
      for (I_ConfigAceFrame frame: AceEditor.getAceFrames()) {
         frame.setStatusMessage(message);
      }
   }

}
