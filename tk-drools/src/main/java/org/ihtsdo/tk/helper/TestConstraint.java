package org.ihtsdo.tk.helper;

import java.util.ArrayList;
import java.util.List;

public class TestConstraint {
	
	List<String> includedModuleIds;

	public TestConstraint() {
		this.includedModuleIds = new ArrayList<String>();
	}

	/**
	 * @return the includedModuleIds
	 */
	public List<String> getIncludedModuleIds() {
		return includedModuleIds;
	}

	/**
	 * @param includedModuleIds the includedModuleIds to set
	 */
	public void setIncludedModuleIds(List<String> includedModuleIds) {
		this.includedModuleIds = includedModuleIds;
	}
	
	public void addModuleId(String moduleId) {
		this.includedModuleIds.add(moduleId);
	}

}
