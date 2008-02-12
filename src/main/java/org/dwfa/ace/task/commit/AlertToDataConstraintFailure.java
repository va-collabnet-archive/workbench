package org.dwfa.ace.task.commit;

import java.util.ArrayList;
import java.util.List;


public class AlertToDataConstraintFailure {

	public enum ALERT_TYPE { INFORMATIONAL, WARNING, ERROR, RESOLVED };
	
	private ALERT_TYPE alertType;
	
	private String alertMessage;
	
	private List<I_Fixup> fixOptions = new ArrayList<I_Fixup>();
	
	public AlertToDataConstraintFailure(ALERT_TYPE alertType,
			String alertMessage) {
		super();
		this.alertType = alertType;
		this.alertMessage = alertMessage;
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
    

}
