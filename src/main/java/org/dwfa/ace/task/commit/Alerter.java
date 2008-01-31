/**
 * 
 */
package org.dwfa.ace.task.commit;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.dwfa.ace.log.AceLog;

public class Alerter implements I_AlertToDataConstraintFailure {
    private Logger log;
    boolean showAlertOnFailure;
    Object selectedOption = null;
    public Alerter(boolean showAlertOnFailure, Logger log) {
        super();
        this.showAlertOnFailure = showAlertOnFailure;
        this.log = log;
    }
    public Alerter(Logger log) {
        super();
        this.showAlertOnFailure = true;
        this.log = log;
    }
    public Alerter() {
        super();
        this.showAlertOnFailure = true;
        this.log = Logger.getLogger(AceLog.getEditLog().getName());
    }

    public void alert(String alertMessage) {
        alert(alertMessage, null);
    }
    public Object alert(final String alertMessage, final Object[] fixOptions) {
        selectedOption = null;
         if (this.showAlertOnFailure) {
            try {
                if (java.awt.EventQueue.isDispatchThread()) {
                    presentAlert(alertMessage, fixOptions);
                } else {
                    SwingUtilities.invokeAndWait(new Runnable() {

                        public void run() {
                            presentAlert(alertMessage, fixOptions);
                        }

                        
                    });
                }
            } catch (InterruptedException e) {
                AceLog.getAppLog().alertAndLogException(e);
            } catch (InvocationTargetException e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
        }
        log.warning("Commit test failed: " + alertMessage);
        return selectedOption;
    }
    private void presentAlert(final String alertMessage, final Object[] fixOptions) {
        JOptionPane.showMessageDialog(null, alertMessage,
                                      "Commit test failed: ",
                                      JOptionPane.ERROR_MESSAGE);
        
        if (fixOptions != null) {
            selectedOption = JOptionPane.showInputDialog(null, "Would you like to apply one \n"+
                                                           "of the following data fixes?",
                                                           "Fixup avaible",
                                                           JOptionPane.QUESTION_MESSAGE,
                                                           null, // do not use a custom icon
                                                           fixOptions,
                                                           fixOptions[0]);
        }
    }

    public Logger getLog() {
        return log;
    }
    public void setLog(Logger log) {
        this.log = log;
    }
    public boolean getShowAlertOnFailure() {
        return showAlertOnFailure;
    }
    
    public void setShowAlertOnFailure(boolean showAlertOnFailure) {
        this.showAlertOnFailure = showAlertOnFailure;
    }
    
}