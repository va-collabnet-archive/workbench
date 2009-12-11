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
package org.dwfa.ace.utypes;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class UniversalAcePath implements Serializable, I_AmChangeSetObject {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private List<UUID> pathId;
	
	private List<UniversalAcePosition> origins;
	
	public UniversalAcePath(List<UUID> pathId, List<UniversalAcePosition> origins) {
		super();
		this.pathId = pathId;
		this.origins = origins;
	}

	public List<UniversalAcePosition> getOrigins() {
		return origins;
	}

	public List<UUID> getPathId() {
		return pathId;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " id: " + pathId + " origins: " + origins;
	}
}
