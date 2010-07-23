package org.ihtsdo.rules;

import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

public class RulesAgenda implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1795953075728434997L;
	HashMap<UUID, String> excludedRules;

	public RulesAgenda() {
		super();
		this.excludedRules = new HashMap<UUID, String>();
	}

	public RulesAgenda(HashMap<UUID, String> excludedRules) {
		super();
		this.excludedRules = excludedRules;
	}

	public HashMap<UUID, String> getExcludedRules() {
		return excludedRules;
	}

	public void setExcludedRules(HashMap<UUID, String> excludedRules) {
		this.excludedRules = excludedRules;
	}
}
