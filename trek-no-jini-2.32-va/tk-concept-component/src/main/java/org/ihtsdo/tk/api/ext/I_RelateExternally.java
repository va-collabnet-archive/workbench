/**
 * Copyright (c) 2012 International Health Terminology Standards Development
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
package org.ihtsdo.tk.api.ext;

import java.util.UUID;

/**
 * The Interface I_RelateExternally.
 */
public interface I_RelateExternally {

	/**
	 * Gets the relationship source uuid.
	 *
	 * @return the relationship source uuid
	 */
	public UUID getRelationshipSourceUuid();

	/**
	 * Gets the relationship target uuid.
	 *
	 * @return the relationship target uuid
	 */
	public UUID getRelationshipTargetUuid();

	/**
	 * Gets the characteristic uuid.
	 *
	 * @return the characteristic uuid
	 */
	public UUID getCharacteristicUuid();

	/**
	 * Gets the refinability uuid.
	 *
	 * @return the refinability uuid
	 */
	public UUID getRefinabilityUuid();

	/**
	 * Gets the relationship group.
	 *
	 * @return the relationship group
	 */
	public int getRelationshipGroup();

	/**
	 * Gets the type uuid.
	 *
	 * @return the type uuid
	 */
	public UUID getTypeUuid();

}