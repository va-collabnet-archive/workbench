package org.ihtsdo.cs.econcept;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Semaphore;

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.cs.I_WriteChangeSet;
import org.dwfa.ace.log.AceLog;
import org.dwfa.util.io.FileIO;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.cs.I_ComputeEConceptForChangeSet;
import org.ihtsdo.db.bdb.BdbProperty;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.time.TimeUtil;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;

public class EConceptChangeSetWriter implements I_WriteChangeSet {
    
    protected static boolean writeDebugFiles = System.getProperty("DEBUG_CS", "false").toLowerCase().startsWith("t");
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    private File changeSetFile;
    
    private File cswcFile;
    private transient FileWriter cswcOut;
    private File csweFile;
    private transient FileWriter csweOut;
    private NidSetBI commitSapNids;

    private File tempFile;

    private transient DataOutputStream tempOut;

	private I_ComputeEConceptForChangeSet computer;
	
	private ChangeSetGenerationPolicy policy;
	
	private Semaphore writePermit = new Semaphore(1);
	
	private boolean timeStampEnabled = true;
	
    public boolean isTimeStampEnabled() {
        return timeStampEnabled;
    }

    public void setTimeStampEnabled(boolean timeStampEnabled) {
        this.timeStampEnabled = timeStampEnabled;
    }

    public EConceptChangeSetWriter(File changeSetFile, File tempFile, 
    		ChangeSetGenerationPolicy policy, boolean timeStampEnabled) {
        super();
        try {
			AceLog.getAppLog().info("EConceptChangeSetWriter boolean timeStampEnabled  " + changeSetFile.getCanonicalPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        this.changeSetFile = changeSetFile;
        this.tempFile = tempFile;
        this.policy = policy;
        this.timeStampEnabled = timeStampEnabled;
    }

    public EConceptChangeSetWriter(File changeSetFile, File tempFile, ChangeSetGenerationPolicy policy) {
        super();
        this.changeSetFile = changeSetFile;
        this.tempFile = tempFile;
        this.policy = policy;
    }

	@Override
	public void open(NidSetBI commitSapNids) throws IOException {
		if (changeSetFile.exists()) {
			 AceLog.getAppLog().info("EConceptChangeSetWriter open cs exists and =  " + changeSetFile.getCanonicalPath());
		   Terms.get().setProperty(changeSetFile.getName(),
		                Long.toString(changeSetFile.length()));
		} else {
		   Terms.get().setProperty(changeSetFile.getName(), "0");
		}
        Terms.get().setProperty(BdbProperty.LAST_CHANGE_SET_WRITTEN.toString(),
                changeSetFile.getName());
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
            cswcOut = new FileWriter(cswcFile, true);

            csweFile = new File(changeSetFile.getParentFile(), changeSetFile.getName() + ".cswe");
            csweOut = new FileWriter(csweFile, true);
        }
	}


	@Override
	public void commit() throws IOException {
		//AceLog.getAppLog().info("EConceptChangeSetWriter commit()");
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
            	//AceLog.getAppLog().info(" tempfile = "+canonicalFileString);
            	if (tempFile.length() > 0) {
                    if (tempFile.renameTo(changeSetFile) == false) {
                        AceLog.getAppLog().warning("tempFile.renameTo failed. Attempting FileIO.copyFile...");
                        FileIO.copyFile(tempFile.getCanonicalPath(), changeSetFile.getCanonicalPath());
                    }
                    //AceLog.getAppLog().info("EConceptChangeSetWriter commit() line 144 where the MAGIC happens");
                    tempFile = new File(canonicalFileString);
            	}
            	
            	//test exist CS file if exists and tempFile is exists & then the post temp2cs file.
            	//AceLog.getAppLog().info("EConceptChangeSetWriter commit() line 149 where tempFile.delete()s");
                tempFile.delete();
            }
            if (changeSetFile.length() == 0) {
                changeSetFile.delete();
            } else {
                AceLog.getAppLog().info("Finished import of: " + changeSetFile.getName() + 
                		" size: " + changeSetFile.length());
                Terms.get().setProperty(changeSetFile.getName(),
                        Long.toString(changeSetFile.length()));
                Terms.get().setProperty(BdbProperty.LAST_CHANGE_SET_WRITTEN.toString(),
                        changeSetFile.getName());
            }
        }
	}
	@Override
	public void writeChanges(ConceptChronicleBI igcd, long time)
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
                cswcOut.append("\n*******************************\n");
                cswcOut.append(TimeUtil.formatDateForFile(time));
                cswcOut.append(" sapNids for commit: ");
                cswcOut.append(commitSapNids.toString());
                cswcOut.append("\n*******************************\n");
                cswcOut.append(c.toLongString());
			}
            if (csweOut != null) {
                csweOut.append("\n*******************************\n");
                csweOut.append(TimeUtil.formatDateForFile(time));
                csweOut.append(" sapNids for commit: ");
                csweOut.append(commitSapNids.toString());
                csweOut.append("\n*******************************\n");
                if (eC != null) {
                    csweOut.append(eC.toString());
                } else {
                    csweOut.append("eC == null");
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
    public void setPolicy(ChangeSetGenerationPolicy policy) {
        this.policy = policy;
    }
}
