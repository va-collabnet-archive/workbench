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
import java.util.regex.Pattern;

import org.dwfa.ace.utypes.UniversalAceDescription;
import org.dwfa.tapi.I_DescribeConceptLocally;
import org.dwfa.tapi.TerminologyException;

public interface I_DescriptionVersioned extends I_AmTermComponent {

	public boolean addVersion(I_DescriptionPart newPart);

	public List<I_DescriptionPart> getVersions();

	public int versionCount();

	public boolean matches(Pattern p);

	public int getConceptId();

	public int getDescId();

	public List<I_DescriptionTuple> getTuples();

	public I_DescriptionTuple getFirstTuple();

	public I_DescriptionTuple getLastTuple();

	public void addTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
			Set<I_Position> positionSet, List<I_DescriptionTuple> matchingTuples,
         boolean addUncommitted);

	public void convertIds(I_MapNativeToNative jarToDbNativeMap);

	public boolean merge(I_DescriptionVersioned jarDesc);

	public Set<TimePathId> getTimePathSet();

	public I_DescribeConceptLocally toLocalFixedDesc();
	
	public UniversalAceDescription getUniversal() throws IOException, TerminologyException;

}
