package org.ihtsdo.workflow.utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.UUID;

import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;



/* 
* @author Jesse Efron
* 
*/
public class DatabaseAccessUtility {
	BufferedWriter log = null;
	private static UUID paths[] = null;
	private static I_TermFactory tf;
	private static I_ConfigAceFrame config = null;
	
	public DatabaseAccessUtility() {
		
	}

	public DatabaseAccessUtility(BufferedWriter l) {
		log = l;
	}
	
	public I_TermFactory createTermFactory()
		throws Exception
	{
		createDatabaseConnection();
		
		setupPaths();
		
		defineConfiguration();
		
		return tf;
	}

	private void defineConfiguration()
	throws Exception
	{
		  config.getDescTypes().add(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid());
		  config.getDescTypes().add(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid());
		  config.getDescTypes().add(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.localize().getNid());

		  config.setDefaultStatus(tf.getConcept((ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid())));

		  config.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
		  config.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());

		  // Assign Configuration
		  tf.setActiveAceFrameConfig(config);
	}

	private void setupPaths()
		throws Exception
	{
		// First Path is Edit Path
		// SNOMED Core: 8c230474-9f11-30ce-9cad-185a96fd03a2
		// Workbench Auxiliary: 2faa9260-8fb2-11db-b606-0800200c9a66
		paths = new UUID[2];
		paths[0] = (UUID)ArchitectonicAuxiliary.Concept.SNOMED_CORE.getUids().iterator().next();
		paths[1] = (UUID)ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids().iterator().next();
	
		for (int i = 0; i < paths.length; i++)
	  	{
		  	config.addViewPosition(tf.newPosition(tf.getPath(paths[i]), Integer.MAX_VALUE));
	  	}
		
		// Edit Path is FIRST UUID in paths[]
		config.addEditingPath(tf.getPath(paths[0]));
	}

	private void createDatabaseConnection()
		throws Exception
	{
		DatabaseSetupConfig dbSetupConfig = new DatabaseSetupConfig();
	
		// Location of VODB
		File vodbDirectory = new File("src/main/resources/berkeley-db");
		System.out.println(vodbDirectory.getAbsolutePath());
		Long cacheSize = Long.getLong("600000000");
		boolean readOnly = false;
	 
		//Open Database
//		log.write("Opening database");
		Terms.openDefaultFactory(vodbDirectory, readOnly, cacheSize);
		tf = Terms.get();

		config = tf.newAceFrameConfig();
		Terms.get().setActiveAceFrameConfig(config);
		Object o = Terms.get().getActiveAceFrameConfig();

	}
}
