package org.dwfa.ace.api;
import java.io.IOException;

import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;

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

}
