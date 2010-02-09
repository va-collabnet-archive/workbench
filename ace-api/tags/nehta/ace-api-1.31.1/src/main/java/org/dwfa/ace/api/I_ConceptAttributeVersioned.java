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

import org.dwfa.ace.utypes.UniversalAceConceptAttributes;
import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.TerminologyException;

public interface I_ConceptAttributeVersioned extends I_AmTermComponent {

	public boolean addVersion(I_ConceptAttributePart part);

	public List<I_ConceptAttributePart> getVersions();

	public int versionCount();

	public int getConId();

	public List<I_ConceptAttributeTuple> getTuples();

	public void convertIds(I_MapNativeToNative jarToDbNativeMap);

	public boolean merge(I_ConceptAttributeVersioned jarCon);

	public Set<TimePathId> getTimePathSet();

   public void addTuples(I_IntSet allowedStatus,
         Set<I_Position> positionSet, List<I_ConceptAttributeTuple> returnTuples);

   public void addTuples(I_IntSet allowedStatus,
         Set<I_Position> positionSet, List<I_ConceptAttributeTuple> returnTuples, boolean addUncommitted);

	public I_ConceptualizeLocally getLocalFixedConcept();
	
	public UniversalAceConceptAttributes getUniversal() throws IOException, TerminologyException;

}
