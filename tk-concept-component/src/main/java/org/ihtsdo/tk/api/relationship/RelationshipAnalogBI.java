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
package org.ihtsdo.tk.api.relationship;

import java.beans.PropertyVetoException;

import org.ihtsdo.tk.api.TypedComponentAnalogBI;

// TODO: Auto-generated Javadoc
/**
 * The Interface RelationshipAnalogBI.
 *
 * @param <A> the generic type
 */
public interface RelationshipAnalogBI<A extends RelationshipAnalogBI>
        extends TypedComponentAnalogBI, RelationshipVersionBI<A> {


	/**
	 * Sets the target nid.
	 *
	 * @param targetNid the new target nid
	 * @throws PropertyVetoException the property veto exception
	 */
	public void setTargetNid(int targetNid) throws PropertyVetoException;
	
	/**
	 * Sets the refinability nid.
	 *
	 * @param refinabilityNid the new refinability nid
	 * @throws PropertyVetoException the property veto exception
	 */
	public void setRefinabilityNid(int refinabilityNid) throws PropertyVetoException;
	
	/**
	 * Sets the characteristic nid.
	 *
	 * @param characteristicNid the new characteristic nid
	 * @throws PropertyVetoException the property veto exception
	 */
	public void setCharacteristicNid(int characteristicNid) throws PropertyVetoException;
	
	/**
	 * Sets the group.
	 *
	 * @param group the new group
	 * @throws PropertyVetoException the property veto exception
	 */
	public void setGroup(int group) throws PropertyVetoException;

}
