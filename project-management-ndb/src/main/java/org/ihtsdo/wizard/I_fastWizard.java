package org.ihtsdo.wizard;

import java.util.HashMap;

public interface I_fastWizard {

		HashMap<String, Object>getData() throws Exception;
		void setKey(String key);
}
