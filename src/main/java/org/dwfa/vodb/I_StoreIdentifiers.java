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
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_Path;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.I_ProcessIdEntries;
import org.dwfa.vodb.types.ThinIdVersioned;

import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;

public interface I_StoreIdentifiers extends I_StoreInBdb {

    public int getMinId() throws DatabaseException;

    public int getMaxId() throws DatabaseException;

    public I_Identify getIdNullOk(int nativeId) throws IOException;

    public List<UUID> nativeToUuid(int nativeId) throws DatabaseException;

    public I_Identify getId(int nativeId) throws IOException;

    public Collection<UUID> getUids(int nativeId) throws TerminologyException, IOException;

    public void writeId(I_Identify id) throws DatabaseException;

    public void deleteId(I_Identify id) throws DatabaseException;

    public int nativeGenerationForUuid(UUID uid, int source, int pathId, int version) throws TerminologyException,
            IOException;

    public int uuidToNativeWithGeneration(Collection<UUID> uids, int source, I_Path idPath, int version)
            throws TerminologyException, IOException;

    public int uuidToNativeWithGeneration(UUID uid, int source, Collection<I_Path> idPaths, int version)
            throws TerminologyException, IOException;

    public void iterateIdEntries(I_ProcessIdEntries processor) throws Exception;

    public I_Identify getId(Collection<UUID> uids) throws TerminologyException, IOException;

    public boolean hasId(Collection<UUID> uids) throws DatabaseException;

    public boolean hasId(UUID uid) throws DatabaseException;

    public int uuidToNativeWithGeneration(UUID uid, int source, I_Path idPath, int version)
            throws TerminologyException, IOException;

    public ThinIdVersioned getId(UUID uid) throws TerminologyException, IOException;

    public int uuidToNative(UUID uid) throws TerminologyException, IOException;

    public int uuidToNative(Collection<UUID> uids) throws TerminologyException, IOException;

    public void logIdDbStats() throws DatabaseException;

    public int getCurrentStatusNid();

    public int getAceAuxillaryNid();

    public I_Identify idEntryToObject(DatabaseEntry key, DatabaseEntry value);

}
