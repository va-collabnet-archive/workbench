package org.dwfa.ace.task.commit;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import org.dwfa.ace.api.I_GetConceptData;


public class AlertToDataConstraintFailure {

	public enum ALERT_TYPE { INFORMATIONAL, WARNING, ERROR, RESOLVED };
	
	private ALERT_TYPE alertType;
	
	private String alertMessage;
	
	private List<I_Fixup> fixOptions = new ArrayList<I_Fixup>();
	
	private transient JComponent rendererComponent;
	
	private I_GetConceptData conceptWithAlert;
	
	public AlertToDataConstraintFailure(ALERT_TYPE alertType,
			String alertMessage, I_GetConceptData conceptWithAlert) {
		super();
		this.alertType = alertType;
		this.alertMessage = alertMessage;
		this.conceptWithAlert = conceptWithAlert;
	}

	public ALERT_TYPE getAlertType() {
		return alertType;
	}

	public String getAlertMessage() {
		return alertMessage;
	}

	public List<I_Fixup> getFixOptions() {
		return fixOptions;
	}

	public JComponent getRendererComponent() {
		return rendererComponent;
	}

	public void setRendererComponent(JComponent rendererComponent) {
		this.rendererComponent = rendererComponent;
	}

	public I_GetConceptData getConceptWithAlert() {
		return conceptWithAlert;
	}
    

}
