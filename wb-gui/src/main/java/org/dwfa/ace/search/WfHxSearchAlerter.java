package org.dwfa.ace.search;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.dwfa.ace.log.AceLog;
import org.dwfa.util.LogWithAlerts;

public class WfHxSearchAlerter {
    private Logger log;
    boolean showAlertOnFailure;
    private Exception ex;
    private AlertToWfHxSearchFailure alert;
    
    public WfHxSearchAlerter(boolean showAlertOnFailure, Logger log, AlertToWfHxSearchFailure alert) {
        this.showAlertOnFailure = showAlertOnFailure;
        this.log = log;
        this.alert = alert;
    }

    public WfHxSearchAlerter(Logger log, AlertToWfHxSearchFailure alert) {
        this.showAlertOnFailure = true;
        this.log = log;
        this.alert = alert;
    }

    public WfHxSearchAlerter(AlertToWfHxSearchFailure alert) {
        this.showAlertOnFailure = true;
        this.log = Logger.getLogger(AceLog.getEditLog().getName());
        this.alert = alert;
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
        log.warning("Commit test " + alert.getAlertType() + ": " + alert.getAlertMessage());
    }

    private void presentAlert() throws Exception {
        JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), alert.getAlertMessage(),
            "Workflow Search Criteria Error:", JOptionPane.ERROR_MESSAGE);

    }
}
