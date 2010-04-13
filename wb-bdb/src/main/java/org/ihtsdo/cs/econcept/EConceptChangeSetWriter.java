package org.ihtsdo.cs.econcept;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Semaphore;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.cs.ChangeSetPolicy;
import org.dwfa.ace.api.cs.I_WriteChangeSet;
import org.dwfa.ace.log.AceLog;
import org.dwfa.util.io.FileIO;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.cs.I_ComputeEConceptForChangeSet;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.time.TimeUtil;

public class EConceptChangeSetWriter implements I_WriteChangeSet {
    
    protected static boolean writeDebugFiles = false;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    private File changeSetFile;
    
    private File cswcFile;
    private transient DataOutputStream cswcOut;
    private File csweFile;
    private transient DataOutputStream csweOut;
    private I_IntSet commitSapNids;

    private File tempFile;

    private transient DataOutputStream tempOut;

	private I_ComputeEConceptForChangeSet computer;
	
	private ChangeSetPolicy policy;
	
	private Semaphore writePermit = new Semaphore(1);

    public EConceptChangeSetWriter(File changeSetFile, File tempFile, ChangeSetPolicy policy) {
        super();
        this.changeSetFile = changeSetFile;
        this.tempFile = tempFile;
        this.policy = policy;
    }

	@Override
	public void open(I_IntSet commitSapNids) throws IOException {
	    this.commitSapNids = commitSapNids;
		computer = new EConceptChangeSetComputer(policy, commitSapNids);
        if (changeSetFile.exists() == false) {
            changeSetFile.getParentFile().mkdirs();
            changeSetFile.createNewFile();
        }
        FileIO.copyFile(changeSetFile.getCanonicalPath(), tempFile.getCanonicalPath());
        AceLog.getAppLog().info(
            "Copying from: " + changeSetFile.getCanonicalPath() + "\n        to: " + tempFile.getCanonicalPath());
        tempOut = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(tempFile, true)));
        if (writeDebugFiles) {
            cswcFile = new File(changeSetFile.getParentFile(), changeSetFile.getName() + ".cswc");
            cswcOut = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(cswcFile, true)));

            csweFile = new File(changeSetFile.getParentFile(), changeSetFile.getName() + ".cswe");
            csweOut = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(csweFile, true)));
        }
	}


	@Override
	public void commit() throws IOException {
        if (tempOut != null) {
            tempOut.flush();
            tempOut.close();
            tempOut = null;
            if (cswcOut != null) {
                cswcOut.flush();
                cswcOut.close();
                cswcOut = null;
                if (cswcFile.length() == 0) {
                    cswcFile.delete();
                }
            }
            if (csweOut != null) {
                csweOut.flush();
                csweOut.close();
                csweOut = null;
                if (csweFile.length() == 0) {
                    csweFile.delete();
                }
            }
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
            if (changeSetFile.length() == 0) {
                changeSetFile.delete();
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
		    EConcept eC = null;
			try {
	            eC = computer.getEConcept(c);
	            writePermit.acquireUninterruptibly();
	            tempOut.writeLong(time);
                eC.writeExternal(tempOut);
            } catch (Throwable e) {
                AceLog.getAppLog().severe("\n##################################################################\n" +
                    "Exception writing change set for concept: \n" + 
                    c.toLongString() + 
                    "\n\neConcept: " + 
                    eC +
                    "\n##################################################################\n"
                    );
                AceLog.getAppLog().alertAndLogException(new Exception("Exception writing change set for: " + c + 
                    "\n See log for details"));
                
            }
			if (cswcOut != null) {
                cswcOut.writeUTF("\n*******************************\n");
                cswcOut.writeUTF(TimeUtil.formatDateForFile(time));
                cswcOut.writeUTF(" sapNids for commit: ");
                cswcOut.writeUTF(commitSapNids.toString());
                cswcOut.writeUTF("\n*******************************\n");
                cswcOut.writeUTF(c.toLongString());
			}
            if (csweOut != null) {
                csweOut.writeUTF("\n*******************************\n");
                csweOut.writeUTF(TimeUtil.formatDateForFile(time));
                csweOut.writeUTF(" sapNids for commit: ");
                csweOut.writeUTF(commitSapNids.toString());
                csweOut.writeUTF("\n*******************************\n");
                if (eC != null) {
                    csweOut.writeUTF(eC.toString());
                } else {
                    csweOut.writeUTF("eC == null");
                }
            }
			writePermit.release();
		}
	}

    @Override
    public String toString() {
        return "EConceptChangeSetWriter: changeSetFile: " + changeSetFile + " tempFile: " + tempFile;
    }

    @Override
    public void setPolicy(ChangeSetPolicy policy) {
        this.policy = policy;
    }
}
