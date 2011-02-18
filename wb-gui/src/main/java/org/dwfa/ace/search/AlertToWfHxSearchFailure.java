package org.dwfa.ace.search;

import javax.swing.JComponent;

import org.dwfa.util.HashFunction;

public class AlertToWfHxSearchFailure {
    public enum ALERT_TYPE {
        INFORMATIONAL, WARNING, ERROR, RESOLVED
    };

    private ALERT_TYPE alertType;

    private String alertMessage;
 
    private transient JComponent rendererComponent;

    private String stringCausingAlert;

    public AlertToWfHxSearchFailure(ALERT_TYPE alertType, String alertMessage, String stringCausingAlert) {
        this.alertType = alertType;
        this.alertMessage = alertMessage;
        this.stringCausingAlert = stringCausingAlert;
    }

    public ALERT_TYPE getAlertType() {
        return alertType;
    }

    public String getAlertMessage() {
        return alertMessage;
    }

    public JComponent getRendererComponent() {
        return rendererComponent;
    }

    public void setRendererComponent(JComponent rendererComponent) {
        this.rendererComponent = rendererComponent;
    }

    public String getStringCausingAlert() {
        return stringCausingAlert;
    }

    @Override
    public boolean equals(Object obj) {
        if (AlertToWfHxSearchFailure.class.isAssignableFrom(obj.getClass())) {
            AlertToWfHxSearchFailure another = (AlertToWfHxSearchFailure) obj;
            if (!alertMessage.equals(another.alertMessage)) {
                return false;
            }
            if (!stringCausingAlert.equals(another.stringCausingAlert)) {
                return false;
            }
            if (!alertType.equals(another.alertType)) {
                return false;
            }
            return true;
        }
        return false;
     }

    @Override
    public int hashCode() {
        return HashFunction.hashCode(new int[] {alertMessage.hashCode(), stringCausingAlert.hashCode() });
    }
}
