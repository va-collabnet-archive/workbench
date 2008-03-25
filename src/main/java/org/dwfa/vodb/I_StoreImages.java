package org.dwfa.vodb;

import java.util.List;

import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.vodb.types.I_ProcessImageEntries;

import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;

public interface I_StoreImages extends I_StoreInBdb {

	public void writeImage(I_ImageVersioned image)
			throws DatabaseException;

	public boolean hasImage(int imageId) throws DatabaseException;

	public I_ImageVersioned getImage(int nativeId)
			throws DatabaseException;

	public List<I_ImageVersioned> getImages(int conceptId)
			throws DatabaseException;
	
	public void iterateImages(I_ProcessImageEntries processor) throws Exception;

	public I_ImageVersioned imageEntryToObject(DatabaseEntry key, DatabaseEntry value);

}