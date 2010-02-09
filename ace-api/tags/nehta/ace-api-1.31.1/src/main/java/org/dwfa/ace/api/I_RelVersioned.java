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
package org.dwfa.ace.api;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.utypes.UniversalAceRelationship;
import org.dwfa.tapi.TerminologyException;

public interface I_RelVersioned extends I_AmTermComponent {

	public boolean addVersion(I_RelPart rel);

	public boolean addVersionNoRedundancyCheck(I_RelPart rel);

	public List<I_RelPart> getVersions();

	public int versionCount();

	public boolean addRetiredRec(int[] releases, int retiredStatusId);

	public boolean removeRedundantRecs();

	/**
	 * 
	 * @return the native id of the source concept (c1) of this relationship
 	 */
	public int getC1Id();
	/**
	 * 
	 * @return the native id of the destination concept (c2) of this relationship
	 */
	public int getC2Id();

	public int getRelId();

	public List<I_RelTuple> getTuples();

	public I_RelTuple getFirstTuple();

	public I_RelTuple getLastTuple();

	public void addTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
			Set<I_Position> positions, List<I_RelTuple> returnRels,
			boolean addUncommitted);

	public void convertIds(I_MapNativeToNative jarToDbNativeMap);

	public boolean merge(I_RelVersioned jarRel);

	public Set<TimePathId> getTimePathSet();

	public void setC2Id(int destId);

	public UniversalAceRelationship getUniversal() throws IOException,
			TerminologyException;

}
