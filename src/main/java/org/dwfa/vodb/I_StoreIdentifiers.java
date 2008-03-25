package org.dwfa.vodb;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_Path;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.I_ProcessIdEntries;
import org.dwfa.vodb.types.ThinIdVersioned;

import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;

public interface I_StoreIdentifiers extends I_StoreInBdb {

	public int getMinId() throws DatabaseException;

	public int getMaxId() throws DatabaseException;

	public I_IdVersioned getIdNullOk(int nativeId) throws IOException;

	public List<UUID> nativeToUuid(int nativeId)
			throws DatabaseException;

	public I_IdVersioned getId(int nativeId) throws IOException;

	public Collection<UUID> getUids(int nativeId)
			throws TerminologyException, IOException;

	public void writeId(I_IdVersioned id) throws DatabaseException;

	public void deleteId(I_IdVersioned id) throws DatabaseException;

	public int nativeGenerationForUuid(UUID uid, int source,
			int pathId, int version) throws TerminologyException, IOException;

	public int uuidToNativeWithGeneration(Collection<UUID> uids,
			int source, I_Path idPath, int version)
			throws TerminologyException, IOException;

	public int uuidToNativeWithGeneration(UUID uid, int source,
			Collection<I_Path> idPaths, int version)
			throws TerminologyException, IOException;

	public void iterateIdEntries(I_ProcessIdEntries processor)
			throws Exception;

	public I_IdVersioned getId(Collection<UUID> uids)
			throws TerminologyException, IOException;

	public boolean hasId(Collection<UUID> uids)
			throws DatabaseException;

	public boolean hasId(UUID uid) throws DatabaseException;

	public int uuidToNativeWithGeneration(UUID uid, int source,
			I_Path idPath, int version) throws TerminologyException,
			IOException;

	public ThinIdVersioned getId(UUID uid)
			throws TerminologyException, IOException;

	public int uuidToNative(UUID uid) throws TerminologyException,
			IOException;

	public int uuidToNative(Collection<UUID> uids)
			throws TerminologyException, IOException;

	public void logIdDbStats() throws DatabaseException;
	
	public int getCurrentStatusNid();
	public int getAceAuxillaryNid();

	public I_IdVersioned idEntryToObject(DatabaseEntry key, DatabaseEntry value);

}