package org.ihtsdo.cs.econcept;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.cs.I_WriteChangeSet;
import org.dwfa.ace.log.AceLog;
import org.dwfa.util.io.FileIO;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.cs.ChangeSetPolicy;
import org.ihtsdo.cs.I_ComputeEConceptForChangeSet;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.time.TimeUtil;

public class EConceptChangeSetWriter implements I_WriteChangeSet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    private File changeSetFile;

    private File tempFile;

    private transient DataOutputStream tempOut;

	private I_ComputeEConceptForChangeSet computer;
	
	private ChangeSetPolicy policy;

    public EConceptChangeSetWriter(File changeSetFile, File tempFile, ChangeSetPolicy policy) {
        super();
        this.changeSetFile = changeSetFile;
        this.tempFile = tempFile;
        this.policy = policy;
    }

	@Override
	public void open(I_IntSet commitSapNids) throws IOException {
		computer = new EConceptChangeSetComputer(policy, commitSapNids);
        if (changeSetFile.exists() == false) {
            changeSetFile.getParentFile().mkdirs();
            changeSetFile.createNewFile();
        }
        FileIO.copyFile(changeSetFile.getCanonicalPath(), tempFile.getCanonicalPath());
        AceLog.getAppLog().info(
            "Copying from: " + changeSetFile.getCanonicalPath() + "\n        to: " + tempFile.getCanonicalPath());
        tempOut = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(tempFile, true)));
	}


	@Override
	public void commit() throws IOException {
        if (tempOut != null) {
            tempOut.flush();
            tempOut.close();
            tempOut = null;
            String canonicalFileString = tempFile.getCanonicalPath();
            if (tempFile.exists()) {
            	if (tempFile.length() > 0) {
                    if (tempFile.renameTo(changeSetFile) == false) {
                        AceLog.getAppLog().warning("tempFile.renameTo failed. Attempting FileIO.copyFile...");
                        FileIO.copyFile(tempFile.getCanonicalPath(), changeSetFile.getCanonicalPath());
                    }
                    tempFile = new File(canonicalFileString);
            	}
                tempFile.delete();
            }
        }
	}
	@Override
	public void writeChanges(I_GetConceptData igcd, long time)
			throws IOException {
		assert time != Long.MAX_VALUE;
		assert time != Long.MIN_VALUE;
		Concept c = (Concept) igcd;
		if (c.isCanceled()) {
	        AceLog.getAppLog().info("Writing canceled concept suppressed: " + 
	        		c.toLongString());
		} else {
			EConcept eC = computer.getEConcept(c);
	        tempOut.writeLong(time);
	        AceLog.getAppLog().info("Write time: " + time + " ("
	        		+ TimeUtil.formatDateForFile(time)+ ")");
	        AceLog.getAppLog().info("eConcept: " + eC.toString());
			eC.writeExternal(tempOut);
		}
	}

    @Override
    public String toString() {
        return "EConceptChangeSetWriter: changeSetFile: " + changeSetFile + " tempFile: " + tempFile;
    }
}
