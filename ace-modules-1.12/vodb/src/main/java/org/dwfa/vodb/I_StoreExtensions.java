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
import java.util.List;

import org.dwfa.ace.api.ebr.I_GetExtensionData;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.vodb.types.ExtensionByReferenceBean;
import org.dwfa.vodb.types.I_ProcessExtByRefEntries;

import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;

public interface I_StoreExtensions extends I_StoreInBdb {

    public void writeExt(I_ThinExtByRefVersioned ext) throws IOException;

    public void iterateExtByRefEntries(I_ProcessExtByRefEntries processor) throws Exception;

    public List<I_ThinExtByRefVersioned> getRefsetExtensionMembers(int refsetId) throws IOException;

    public List<ExtensionByReferenceBean> getExtensionsForRefset(int refsetId) throws IOException;

    public List<I_ThinExtByRefVersioned> getAllExtensionsForComponent(int componentId) throws IOException;

    public List<I_GetExtensionData> getExtensionsForComponent(int componentId) throws IOException;

    public I_ThinExtByRefVersioned getExtension(int memberId) throws IOException;

    public boolean hasExtension(int memberId) throws IOException;

    public I_ThinExtByRefVersioned extEntryToObject(DatabaseEntry key, DatabaseEntry value);

}
