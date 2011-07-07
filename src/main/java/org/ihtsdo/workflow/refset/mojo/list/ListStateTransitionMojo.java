package org.ihtsdo.workflow.refset.mojo.list;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.stateTrans.StateTransitionRefsetReader;

/**
 * @author Jesse Efron
 * 
 * @goal list-state-transition-refset
 * @requiresDependencyResolution compile
 */

public class ListStateTransitionMojo extends AbstractMojo {

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
           

            StateTransitionRefsetReader reader = new StateTransitionRefsetReader();
            HashSet<String> members = getRefsetMembers(reader.getRefsetNid());
            
            printRefsetMembers(members);
		} catch (Exception e) {
			AceLog.getAppLog().log(Level.WARNING, "Unable to read state transition refset with error: " + e.getMessage());
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

                	results.add(refCompId + " *with* " + part.getStringValue());
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
