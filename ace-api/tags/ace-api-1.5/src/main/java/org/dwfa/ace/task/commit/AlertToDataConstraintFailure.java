/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
