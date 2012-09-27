/*
 * Copyright (c) 2010 International Health Terminology Standards Development
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
package org.ihtsdo.rules;

import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

/**
 * The Class RulesAgenda.
 */
public class RulesAgenda implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1795953075728434997L;
	
	/** The excluded rules. */
	HashMap<UUID, String> excludedRules;

	/**
	 * Instantiates a new rules agenda.
	 */
	public RulesAgenda() {
		super();
		this.excludedRules = new HashMap<UUID, String>();
	}

	/**
	 * Instantiates a new rules agenda.
	 *
	 * @param excludedRules the excluded rules
	 */
	public RulesAgenda(HashMap<UUID, String> excludedRules) {
		super();
		this.excludedRules = excludedRules;
	}

	/**
	 * Gets the excluded rules.
	 *
	 * @return the excluded rules
	 */
	public HashMap<UUID, String> getExcludedRules() {
		return excludedRules;
	}

	/**
	 * Sets the excluded rules.
	 *
	 * @param excludedRules the excluded rules
	 */
	public void setExcludedRules(HashMap<UUID, String> excludedRules) {
		this.excludedRules = excludedRules;
	}
}
