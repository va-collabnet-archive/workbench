package org.ihtsdo.workflow.refset.mojo.list;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.semArea.SemanticAreaSearchRefset;

/**
 * @author Jesse Efron
 * 
 * @goal list-semantic-area-search-refset
 * @requiresDependencyResolution compile
 */

public class ListSemanticAreaSearchMojo extends AbstractMojo {

    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;


    private HashMap<String, I_GetConceptData> modelers = null;
    
    public void execute() throws MojoExecutionException, MojoFailureException 
    {
        System.setProperty("java.awt.headless", "true");
        try {
            SemanticAreaSearchRefset refset = new SemanticAreaSearchRefset();
            HashSet<String> members = getRefsetMembers(refset.getRefsetId());            
            printRefsetMembers(members);
		} catch (Exception e) {
			e.printStackTrace();
			e.getMessage();
			throw new MojoExecutionException(e.getMessage());
		}
	}
    public File getTargetDirectory() {
        return targetDirectory;
	}

    public void setTargetDirectory(File targetDirectory) {
        this.targetDirectory = targetDirectory;
	}
    
    private I_GetConceptData getEditor(String editor) {
    	return modelers.get(editor);
    }

    private HashSet<String> getRefsetMembers(int refsetId) throws TerminologyException, IOException {
        HashSet<String> results = new HashSet<String>();

        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

        Collection<? extends I_ExtendByRef> extVersions = Terms.get().getRefsetExtensionMembers(refsetId);
        
        for (I_ExtendByRef thinExtByRefVersioned : extVersions) {

            List<? extends I_ExtendByRefVersion> extensions = thinExtByRefVersioned.getTuples(config.getAllowedStatus(),
                config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy());

            for (I_ExtendByRefVersion thinExtByRefTuple : extensions) {
                if (thinExtByRefTuple.getRefsetId() == refsetId) {

                	int refCompId = thinExtByRefTuple.getComponentId();
                	I_ExtendByRefPartStr part = (I_ExtendByRefPartStr) thinExtByRefTuple.getMutablePart();

                    if (part.getStringValue() == null) {
                    	System.out.println("AAA");
                    } else if (part.getStringValue().length() == 0) {
                    	System.out.println("BBB");
                    } else {
                    	results.add(refCompId + " *with* " + part.getStringValue());
                    }
                }
            }
        }

        return results;
    }

    private void printRefsetMembers(HashSet<String> members) {
    	Iterator itr = members.iterator();
    	while (itr.hasNext()) {
    		System.out.println("Next: " + ((String)itr.next()));
    	}
    }

}
