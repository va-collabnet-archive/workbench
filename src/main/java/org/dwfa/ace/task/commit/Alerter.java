/**
 * 
 */
package org.dwfa.ace.task.commit;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.dwfa.ace.log.AceLog;

public class Alerter  {
    private Logger log;
    boolean showAlertOnFailure;
	private AlertToDataConstraintFailure alert;
	private Exception ex;
    public Alerter(boolean showAlertOnFailure, Logger log, AlertToDataConstraintFailure alert) {
        super();
        this.showAlertOnFailure = showAlertOnFailure;
        this.log = log;
        this.alert = alert;
    }
    public Alerter(Logger log, AlertToDataConstraintFailure alert) {
        super();
        this.showAlertOnFailure = true;
        this.log = log;
        this.alert = alert;
    }
    public Alerter(AlertToDataConstraintFailure alert) {
        super();
        this.showAlertOnFailure = true;
        this.log = Logger.getLogger(AceLog.getEditLog().getName());
        this.alert = alert;
    }

    public void alert() throws Exception {
         if (this.showAlertOnFailure) {
            try {
                if (java.awt.EventQueue.isDispatchThread()) {
                    presentAlert();
                } else {
                    SwingUtilities.invokeAndWait(new Runnable() {

                        public void run() {
                            try {
								presentAlert();
							} catch (Exception e) {
								ex = e;
							}
                        }

                        
                    });
                    if (ex != null) {
                    	throw ex;
                    }
                }
            } catch (InterruptedException e) {
                AceLog.getAppLog().alertAndLogException(e);
            } catch (InvocationTargetException e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
        }
        log.warning("Commit test " + alert.getAlertType() + 
        		": " + alert.getAlertMessage());
    }
    private void presentAlert() throws Exception {
        JOptionPane.showMessageDialog(null, alert.getAlertMessage(),
                                      "Commit test failed: ",
                                      JOptionPane.ERROR_MESSAGE);
        
        if (alert.getFixOptions() != null && alert.getFixOptions().size() > 0) {
        	I_Fixup selectedOption = (I_Fixup) JOptionPane.showInputDialog(null, "Would you like to apply one \n"+
                                                           "of the following data fixes?",
                                                           "Fixup avaible",
                                                           JOptionPane.QUESTION_MESSAGE,
                                                           null, // do not use a custom icon
                                                           alert.getFixOptions().toArray(),
                                                           alert.getFixOptions().get(0));
        	if (selectedOption != null) {
        		selectedOption.fix();
        	}
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