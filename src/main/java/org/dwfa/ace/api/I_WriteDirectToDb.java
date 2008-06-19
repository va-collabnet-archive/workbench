package org.dwfa.ace.api;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.tapi.TerminologyException;

/**
 * Methods in this interface write directly to the database, and bypass the
 * change set generation and transactional model. You still have to perform a 
 * sync operation to ensure that the data persists, but there is no guaranteed
 * rollback capability. 
 * 
 * @author kec
 *
 */
public interface I_WriteDirectToDb {

	/**
	 * Writes images directly to the database. <br><br>
	 * This method bypasses change set generation 
	 * and the transactional model.
	 * You still have to perform a 
     * sync operation to ensure that the data persists
	 * 
	 * @param image
	 * @throws IOException
	 */
	public void writeImage(I_ImageVersioned image) throws IOException;

	/**
	 * Write concept attributes directly to the database. <br><br>
	 * This method bypasses change set generation 
	 * and the transactional model.
	 * You still have to perform a 
     * sync operation to ensure that the data persists
	 * @param concept
	 * @throws IOException
	 */
	public void writeConceptAttributes(I_ConceptAttributeVersioned concept) throws IOException;

	/**
	 * Write relationships directly to the database. <br><br>
	 * This method bypasses change set generation 
	 * and the transactional model.
	 * You still have to perform a 
     * sync operation to ensure that the data persists
	 * @param rel
	 * @throws IOException
	 */
	public void writeRel(I_RelVersioned rel) throws IOException;

	/**
	 * Write extensions directly to the database. <br><br>
	 * This method bypasses change set generation 
	 * and the transactional model.
	 * You still have to perform a 
     * sync operation to ensure that the data persists
	 * @param ext
	 * @throws IOException
	 */
	public void writeExt(I_ThinExtByRefVersioned ext) throws IOException;

	/**
	 * write descriptions directly to the database. <br><br>
	 * This method bypasses change set generation 
	 * and the transactional model.
	 * You still have to perform a 
     * sync operation to ensure that the data persists
	 * @param desc
	 * @throws IOException
	 */
	public void writeDescription(I_DescriptionVersioned desc) throws IOException;

	/**
	 * Write path directly to the database. <br><br>
	 * This method bypasses change set generation 
	 * and the transactional model.
	 * You still have to perform a 
     * sync operation to ensure that the data persists
	 * Write paths directly to the database. 
	 * @param p
	 * @throws IOException
	 */
	public void writePath(I_Path p) throws IOException;
	
	/**
	 * Ensures that all changes are checkpointed to the database. 
	 * @throws IOException
	 */
	public void sync() throws IOException;
	
	/**
	 * Adds an entry to the time/path database. This entry is necessary for the 
	 * time/path to show up in the preferences panel. 
	 * @param timePath
	 * @throws IOException
	 */
	
	public void writeTimePath(TimePathId timePath) throws IOException;
	/**
	 * Write identifiers directly to the database. <br><br>
	 * This method bypasses change set generation 
	 * and the transactional model.
	 * You still have to perform a 
     * sync operation to ensure that the data persists. 
     * Use this method with great care. If you don't properly generate 
     * native identifiers, you will corrupt the database. 
	 * @param id
	 * @throws IOException
	 */
	public void writeId(I_IdVersioned id) throws IOException;
	
	/**
	 * Generate a new native identifier in the database from the provided information. 
	 * @param uids
	 * @param source
	 * @param idPath
	 * @param version
	 * @return
	 * @throws TerminologyException
	 * @throws IOException
	 */
	public int uuidToNativeDirectWithGeneration(Collection<UUID> uids, int source,
			I_Path idPath, int version) throws TerminologyException,
			IOException;
	
	/**
	 * The new routines in the I_TermFactory automatically put the new components
	 * on the commit list, and creates new threads to update the interface. This method
	 * bypasses the commit list. 
	 * 
	 * @param refsetId
	 * @param memberId
	 * @param componentId
	 * @param typeId
	 * @return
	 */
	public I_ThinExtByRefVersioned newExtensionBypassCommit(int refsetId,
			int memberId, int componentId, int typeId);
	
	/**
	 * The new routines in the I_TermFactory automatically put the new components
	 * on the commit list, and creates new threads to update the interface. This method
	 * bypasses the commit list. 
	 * 
	 * @param conceptNid
	 * @return
	 * @throws IOException
	 */
	public I_GetConceptData newConceptBypassCommit(int conceptNid) throws IOException;
	
	/**
	 * The new routines in the I_TermFactory automatically put the new components
	 * on the commit list, and creates new threads to update the interface. This method
	 * bypasses the commit list. 
	 * 
	 * @param descriptionNid
	 * @param conceptNid
	 * @return
	 * @throws IOException
	 */
	public I_DescriptionVersioned newDescriptionBypassCommit(int descriptionNid,
															 int conceptNid) throws IOException;
	
	/**
	 * The new routines in the I_TermFactory automatically put the new components
	 * on the commit list, and creates new threads to update the interface. This method
	 * bypasses the commit list. 
	 * 
	 * @param relNid
	 * @param conceptNid
	 * @param relDestinationNid
	 * @return
	 * @throws IOException
	 */

	public I_RelVersioned newRelationshipBypassCommit(int relNid,
													  int conceptNid, int relDestinationNid) throws IOException;
	/**
	 * Generates a new I_IdVersioned, without putting the new object on the commit list. 
	 * @param nid
	 * @return
	 */
	public I_IdVersioned newIdVersionedBypassCommit(int nid);
	
	public I_IdPart newIdPart();

}
