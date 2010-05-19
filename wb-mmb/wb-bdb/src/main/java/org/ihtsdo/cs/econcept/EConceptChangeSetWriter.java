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
import org.dwfa.vodb.types.IntSet;
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
	
	private  boolean processNidLists;
	
	private boolean timeStampEnabled = true;
	
    public boolean isTimeStampEnabled() {
        return timeStampEnabled;
    }

    public void setTimeStampEnabled(boolean timeStampEnabled) {
        this.timeStampEnabled = timeStampEnabled;
    }

    public EConceptChangeSetWriter(File changeSetFile, File tempFile, ChangeSetPolicy policy, 
            boolean processNidLists, boolean timeStampEnabled) {
        super();
        this.changeSetFile = changeSetFile;
        this.tempFile = tempFile;
        this.policy = policy;
        this.processNidLists = processNidLists;
        this.timeStampEnabled = timeStampEnabled;
    }

    public EConceptChangeSetWriter(File changeSetFile, File tempFile, ChangeSetPolicy policy, boolean processNidLists) {
        super();
        this.changeSetFile = changeSetFile;
        this.tempFile = tempFile;
        this.policy = policy;
        this.processNidLists = processNidLists;
    }

	@Override
	public void open(I_IntSet commitSapNids) throws IOException {
	    this.commitSapNids = commitSapNids;
		computer = new EConceptChangeSetComputer(policy, (IntSet) commitSapNids, processNidLists);
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
	        long start = System.currentTimeMillis();
			try {
	            eC = computer.getEConcept(c);
	            if (eC != null) {
	                long computeTime = System.currentTimeMillis() - start;
	                writePermit.acquireUninterruptibly();
	                long permitTime = System.currentTimeMillis() - start - computeTime;
	                if (timeStampEnabled) {
	                    tempOut.writeLong(time);
	                }
	                eC.writeExternal(tempOut);
	                long writeTime = System.currentTimeMillis() - start - permitTime - computeTime;
	                long totalTime = System.currentTimeMillis() - start;
	                if (totalTime > 100000) {
	                    AceLog.getAppLog().info("\n##################################################################\n" +
	                        "Exceptional change set write time for concept: \n" + 
	                        "\nCompute time: " + TimeUtil.getElapsedTimeString(computeTime) + 
	                        "\nPermit time: " + TimeUtil.getElapsedTimeString(permitTime) + 
	                        "\nWrite time: " + TimeUtil.getElapsedTimeString(writeTime) + 
	                        "\nTotal time: " + TimeUtil.getElapsedTimeString(totalTime) + 
	                        "\n\neConcept: " + 
	                        eC +
	                        "\n##################################################################\n" +
	                        "\n\nConcept: " + 
	                        c.toLongString() +
	                        "\n##################################################################\n"
	                        );
	                }
	            }
            } catch (Throwable e) {
                AceLog.getAppLog().severe("\n##################################################################\n" +
                    "Exception writing change set for concept: \n" + 
                    c.toLongString() + 
                    "\n\neConcept: " + 
                    eC +
                    "\n##################################################################\n"
                    );
                AceLog.getAppLog().alertAndLogException(new Exception("Exception writing change set for: " + c + 
                    "\n See log for details", e));
                
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
