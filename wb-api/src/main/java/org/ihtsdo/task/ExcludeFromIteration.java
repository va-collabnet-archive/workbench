/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright 
 * protection in the United States. Foreign copyrights may apply.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.task;

import java.util.HashSet;
import java.util.UUID;
import org.dwfa.ace.api.I_GetConceptData;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeVersionBI;

/**
 * {@link ExcludeFromIteration}
 * 
 * A complete hack class used to keep the QA and Classifier from operating on the non-snomed terminologies.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class ExcludeFromIteration
{
	private static UUID[] nonSCTPathConcepts = new UUID[] {UUID.fromString("b2b1cc96-9ca6-5513-aad9-aa21e61ddc29"),  //Loinc
			UUID.fromString("e8feddad-6010-5524-9d02-627821d81705"), //problem list 
			UUID.fromString("8d0add3e-0f30-532a-921b-41b67989565d"),  //vhat
			UUID.fromString("d1cfff32-d25d-57e6-8cc4-fd433cd1096d"),  //rxnorm
			UUID.fromString("dc0417c1-ac6a-5c2d-8fcd-587a534cd42c"),  //ndfrt
			UUID.fromString("58febb9c-97b4-556f-af86-aa230c9b4d19"),  //ndf
			UUID.fromString("cb944979-7038-5e09-94ea-57056bcdbda2"),  //chdr
			UUID.fromString("57eba050-ffdb-573e-97ef-3aa0868a2c71"),  //umls
			UUID.fromString("586ea679-073e-541f-ab10-bbcdaaade4e7"),  //ucum
		}; 
	
	private static HashSet<Integer> pathNidsToSkip = null;
	
	static
	{
		pathNidsToSkip = new HashSet<Integer>();
		
		for (UUID uuid : nonSCTPathConcepts)
		{
			try
			{
				if (Ts.get().hasUuid(uuid)) {
					pathNidsToSkip.add(Ts.get().getNidForUuids(uuid));
				}
			}
			catch (Exception e)
			{
				System.err.println("--- PROBLEM CONFIGURING PATH SKIP SET -----");
				e.printStackTrace();
			}
		}
		
		System.out.println(" ------ Configured " + pathNidsToSkip.size() + " paths into the skip list for iteration --------");
	}
	
	/**
	 * If any version of this concept was created on a path we don't want, 
	 * skip it.
	 * @param data
	 * @return
	 */
	public static boolean exclude(I_GetConceptData data)
	{
		try
		{
			for (ConceptAttributeVersionBI<?> cv : data.getConceptAttributes().getVersions())
			{
				if (pathNidsToSkip.contains(cv.getPathNid()))
				{
					return true;
				}
			}
			return false;
		}
		catch (Exception e)
		{
			System.err.println("Failure during exclude check!");
			e.printStackTrace();
			return false;
		}
	}
}
