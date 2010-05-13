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

import org.dwfa.ace.api.I_Path;
import org.dwfa.vodb.types.I_ProcessPathEntries;

import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;

@Deprecated
public interface I_StorePaths extends I_StoreInBdb {

    public void writePath(I_Path p) throws DatabaseException;

    public I_Path getPath(int nativeId) throws DatabaseException;

    public boolean hasPath(int nativeId) throws DatabaseException;

    public void iteratePaths(I_ProcessPathEntries processor) throws Exception;

    public I_Path pathEntryToObject(DatabaseEntry key, DatabaseEntry value);

}
