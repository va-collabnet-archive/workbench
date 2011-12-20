package org.ihtsdo.wizard;

import java.util.HashMap;

public interface I_notifyPanelChange {
	void notifyThis(WizardFrame wizardFrame,int index,HashMap<String,Object> mapCollector);
}
