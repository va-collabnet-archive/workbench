package org.dwfa.mojo.refset;

import java.util.HashMap;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.mojo.ImportFromFile;
import org.dwfa.maven.MojoUtil;

/**
 * Imports the contents of a file as a sequence of extensions and adds them to an existing refset in the database.
 * 
 * @see https://mgr.cubit.aceworkspace.net/pbl/cubitci/pub/ace-mojo/site/dataimport.html
 * @goal load-refset-file
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class ImportRefset extends ImportFromFile {
	
	
	/**
	 * May be provided to overwrite the destination refset details from the file being imported
	 * 
	 * @parameter
	 */
	private DestinationRefsetDescriptor destination;
	
	/** An internal cache of the new uncommitted extensions, indexed by the member id */ 
	private HashMap<Integer, I_ThinExtByRefVersioned> extensions = new HashMap<Integer, I_ThinExtByRefVersioned>();

	/*
	 * Mojo execution method.
	 * @see org.apache.maven.plugin.AbstractMojo#execute()
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		
		try {
			if (MojoUtil.alreadyRun(getLog(), fileHandler.getSourceFile().toString())) {
				return;
			}

			for (I_ThinExtByRefVersioned extension : fileHandler) {
				
				Integer memberId = extension.getMemberId();
				if (extensions.containsKey(memberId)) {

					// Keep only the latest version part, superseding older parts
					// we have already encountered
					
					I_ThinExtByRefVersioned existingExt = extensions.get(memberId);					
					I_ThinExtByRefPart extPart = existingExt.getVersions().get(0);
					I_ThinExtByRefPart newPart = extension.getVersions().get(0);
					if (extPart.getVersion() <= newPart.getVersion()) {
						existingExt.getVersions().clear();
						existingExt.addVersion(newPart);
					}
					
				} else {
					
					extensions.put(memberId, extension);
					
				}
				
			}
			
			I_TermFactory termFactory = LocalVersionedTerminology.get();
			
			// Add all extensions to be committed
			for (I_ThinExtByRefVersioned extension : extensions.values()) {
				
				// Modify the version part (there should be only one) and set it back anew 
				
				I_ThinExtByRefPart extPart = extension.getVersions().get(0);
				extension.getVersions().remove(0);
				extPart.setVersion(Integer.MAX_VALUE); // uncommitted 
				
				if (destination != null) {
					extPart.setPathId(destination.getPath().getVerifiedConcept().getId().getNativeId());
					extension.setRefsetId(destination.getRefset().getVerifiedConcept().getId().getNativeId());
				}
				
				extension.addVersion(extPart);
				termFactory.addUncommitted(extension);
			}
			
			termFactory.commit();

		} catch (Exception e) {
			String errMsg = "Failed to import refset " + fileHandler.getSourceFile().getName() + " : " + e.getMessage();
			throw new MojoExecutionException(errMsg, e);
		}
		
	}


}