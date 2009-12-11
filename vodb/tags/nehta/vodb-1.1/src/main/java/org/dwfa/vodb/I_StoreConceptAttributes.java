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
package org.dwfa.vodb;

import java.io.IOException;
import java.util.Iterator;

import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.vodb.types.I_ProcessConceptAttributeEntries;

import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;

public interface I_StoreConceptAttributes extends I_StoreInBdb {

	public void writeConceptAttributes(
			I_ConceptAttributeVersioned concept) throws DatabaseException, IOException;

	public boolean hasConcept(int conceptId) throws DatabaseException;

	public I_ConceptAttributeVersioned getConceptAttributes(
			int conceptId) throws IOException;

	public Iterator<I_GetConceptData> getConceptIterator()
			throws IOException;

	public void iterateConceptAttributeEntries(
			I_ProcessConceptAttributeEntries processor) throws Exception;

	public I_ConceptAttributeVersioned conAttrEntryToObject(DatabaseEntry key, DatabaseEntry value);

	public int getConceptCount() throws DatabaseException;

}
