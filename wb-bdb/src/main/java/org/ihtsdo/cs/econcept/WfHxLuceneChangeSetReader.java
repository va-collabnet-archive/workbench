package org.ihtsdo.cs.econcept;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.api.cs.I_Count;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.dwfa.ace.api.cs.I_ValidateChangeSetChanges;
import org.dwfa.ace.exceptions.ToIoException;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.db.bdb.BdbProperty;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.helper.time.TimeHelper;
import org.ihtsdo.lucene.WfHxLuceneWriterAccessor;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

/**
 * Read Changeset files already imported searching for WfHx records in the changesets.
 * If found, add changes to WfHxLucene Directory.
 * 
 * @author Jesse Efron
 */
public class WfHxLuceneChangeSetReader implements I_ReadChangeSet {
    private static final long serialVersionUID = 1L;

    private File changeSetFile;
    private File csreFile;
    private File csrcFile;
    private DataInputStream dataStream;
    private transient FileWriter csreOut;
    private transient FileWriter csrcOut;

    private I_Count counter;

    private int count = 0;
    private int conceptCount = 0;
    private Long nextCommit;
    private boolean noCommit = false;
    private boolean initialized = false;
    private String nextCommitStr;
	private final String wfPropertySuffix = "-WF";
	private UUID workflowHistoryRefsetUid = WorkflowHelper.getWorkflowRefsetUid();
	private static File  firstFileRead = null;

    private transient List<I_ValidateChangeSetChanges> validators = new ArrayList<I_ValidateChangeSetChanges>();
	private static HashSet<TkRefsetAbstractMember<?>> wfMembersToCommit = new HashSet<TkRefsetAbstractMember<?>>();

	public WfHxLuceneChangeSetReader() {

	}

   @Override
    public long nextCommitTime() throws IOException, ClassNotFoundException {
        lazyInit();
        if ((firstFileRead != null) && (firstFileRead.equals(changeSetFile))) {
        	return Long.MAX_VALUE;
        }
        
    	if (nextCommit == null) {
            try {
                nextCommit = dataStream.readLong();
                assert nextCommit != Long.MAX_VALUE;
                nextCommitStr = TimeHelper.getFileDateFormat().format(new Date(nextCommit));
            } catch (EOFException e) {
                AceLog.getAppLog().info("No next commit time for file: " + changeSetFile);
                nextCommit = Long.MAX_VALUE;
                nextCommitStr = "end of time";
            }
        }

    	return nextCommit;
    }

   @Override
    public void readUntil(long endTime) throws IOException, ClassNotFoundException {
        HashSet<TimePathId> values = new HashSet<TimePathId>();

        if ((firstFileRead != null) && (firstFileRead.equals(changeSetFile))) {
        	updateLuceneIndex();
        	firstFileRead = null;
        	return;
        } 

        while ((nextCommitTime() <= endTime) && (nextCommitTime() != Long.MAX_VALUE)) {
            try {
                EConcept eConcept = new EConcept(dataStream);
                if (csreOut != null) {
                    csreOut.append("\n*******************************\n");
                    csreOut.append(TimeHelper.formatDateForFile(nextCommitTime()));
                    csreOut.append("\n*******************************\n");
                    csreOut.append(eConcept.toString());
                }

                count++;
                if (counter != null) {
                    counter.increment();
                }

                 updateWfHxLuceneIndex(eConcept, nextCommit, values);
                conceptCount++;
                nextCommit = dataStream.readLong();
            } catch (EOFException ex) {
                dataStream.close();
                if (changeSetFile.length() == 0) {
                    changeSetFile.delete();
                }
                nextCommit = Long.MAX_VALUE;
                Terms.get().setProperty(changeSetFile.getName() + wfPropertySuffix,
                    Long.toString(changeSetFile.length()));
                Terms.get().setProperty(BdbProperty.LAST_CHANGE_SET_READ.toString() + wfPropertySuffix,
                        changeSetFile.getName());
                if (csreOut != null) {
                    csreOut.flush();
                    csreOut.close();
                    csreFile.delete();
                }
                if (csrcOut != null) {
                    csrcOut.flush();
                    csrcOut.close();
                    csrcFile.delete();
                }
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
        
        if (firstFileRead == null) {
        	firstFileRead = changeSetFile;
        }
   		

    }

   @Override
    public void read() throws IOException, ClassNotFoundException {
        readUntil(Long.MAX_VALUE);
    }

    private void lazyInit() throws FileNotFoundException, IOException, ClassNotFoundException {
        String lastImportSize = Terms.get().getProperty(changeSetFile.getName() + wfPropertySuffix);
        if (lastImportSize != null) {
            long lastSize = Long.parseLong(lastImportSize);
            if (lastSize == changeSetFile.length()) {
                nextCommit = Long.MAX_VALUE;
                initialized = true;
            }
        }
        if (initialized == false) {
            FileInputStream fis = new FileInputStream(changeSetFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            dataStream = new DataInputStream(bis);

            if (EConceptChangeSetWriter.writeDebugFiles) {
                csreFile = new File(changeSetFile.getParentFile(), changeSetFile.getName() + ".csre");
                csreOut = new FileWriter(csreFile, true);
                csrcFile = new File(changeSetFile.getParentFile(), changeSetFile.getName() + ".csrc");
                csrcOut = new FileWriter(csrcFile, true);
            }
            initialized = true;
        }
    }

   @Override
    public File getChangeSetFile() {
        return changeSetFile;
    }

   @Override
    public void setChangeSetFile(File changeSetFile) {
        this.changeSetFile = changeSetFile;
    }

   @Override
    public void setCounter(I_Count counter) {
        this.counter = counter;
    }


   @Override
    public List<I_ValidateChangeSetChanges> getValidators() {
        return validators;
    }

   @Override
    public int availableBytes() throws FileNotFoundException, IOException, ClassNotFoundException {
        lazyInit();
        if ((firstFileRead != null) && (firstFileRead.equals(changeSetFile))) {
        	return 0;
        } else {
	        if (dataStream != null) {
	            return dataStream.available();
	        }
        }
        
        return 0;
    }

   	public boolean isNoCommit() {
       	return noCommit;
   	}

   	public void setNoCommit(boolean noCommit) {
   		this.noCommit = noCommit;
   	}


   	private void updateLuceneIndex() {
		if (wfMembersToCommit.size() > 0) {
			try {
				Runnable  luceneWriter = WfHxLuceneWriterAccessor.prepareWriterWithEConcept(wfMembersToCommit);
				if (luceneWriter != null) {
	   				luceneWriter.run();
	            }        
			} catch (InterruptedException e) {
		        AceLog.getAppLog().log(Level.WARNING, "Failed to generate WfHx Lucene Index on Change Set Import");
			}
		}
	}

   	private void updateWfHxLuceneIndex(EConcept eConcept, long time, Set<TimePathId> values) throws IOException, ClassNotFoundException {
	   try {
	       assert time != Long.MAX_VALUE;
	       List<TkRefsetAbstractMember<?>> members = null;
	       
	       if ((eConcept.getRefsetMembers() != null) &&!eConcept.getRefsetMembers().isEmpty()) {
	    	   members = eConcept.getRefsetMembers();
	       } else if (eConcept.getConceptAttributes() != null && eConcept.getConceptAttributes().getAnnotations() != null) {
	    	   members = eConcept.getConceptAttributes().getAnnotations();
	       } 

	       if (members != null) {
		       for (TkRefsetAbstractMember<?>  member : members) {
	   				if (member.getRefsetUuid().equals(workflowHistoryRefsetUid)) {
	   					try {
	   						if (!WorkflowHelper.getLuceneChangeSetStorage().contains(member.getPrimordialComponentUuid()))	
	   						{
	   							wfMembersToCommit.add(member);
	   							WorkflowHelper.getLuceneChangeSetStorage().add(member.getPrimordialComponentUuid());
		       				}
		   				} catch (Exception e) {
		   		            AceLog.getAppLog().log(Level.WARNING, "Failed getting extension with memberId: " + member.getPrimordialComponentUuid());
		   			    }
		   			}
		       }
	       }
	   } catch (Exception e) {
	       AceLog.getEditLog().severe(
	           "Error committing bean in change set: " + changeSetFile + "\nUniversalAceBean:  \n" + eConcept);
	       throw new ToIoException(e);
	   }
    }

	@Override
	public int getConceptsImported() {
		return conceptCount;
	}
}
