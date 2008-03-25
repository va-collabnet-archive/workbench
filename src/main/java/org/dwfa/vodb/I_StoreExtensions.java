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

	public void writeExt(I_ThinExtByRefVersioned ext)
			throws DatabaseException, IOException;

	public void iterateExtByRefEntries(
			I_ProcessExtByRefEntries processor) throws Exception;

	public List<I_ThinExtByRefVersioned> getRefsetExtensionMembers(
			int refsetId) throws IOException;

	public List<ExtensionByReferenceBean> getExtensionsForRefset(
			int refsetId) throws DatabaseException;

	public List<I_ThinExtByRefVersioned> getAllExtensionsForComponent(
			int componentId) throws IOException;

	public List<I_GetExtensionData> getExtensionsForComponent(
			int componentId) throws IOException;

	public I_ThinExtByRefVersioned getExtension(int memberId)
			throws IOException;

	public boolean hasExtension(int memberId) throws DatabaseException;

	public I_ThinExtByRefVersioned extEntryToObject(DatabaseEntry key, DatabaseEntry value);

}