package org.dwfa.ace.task.wfdetailsSheet;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/gui/workflow/detail sheet", type = BeanType.TASK_BEAN) })
public class SetWorkflowDetailsSheetToRefreshSpecClausePanel extends AbstractTask {
    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();
    private String refsetUuidPropName = ProcessAttachmentKeys.WORKING_REFSET.getAttachmentKey();
    private String refsetPositionSetPropName = ProcessAttachmentKeys.POSITION_SET.getAttachmentKey();

	private String snomedPositionSetPropName = ProcessAttachmentKeys.POSITION_LIST.getAttachmentKey();
    private String conceptToReplaceUuidPropName = ProcessAttachmentKeys.CONCEPT_TO_REPLACE_UUID.getAttachmentKey();
    private String clauseToUpdateMemberUuidPropName = ProcessAttachmentKeys.REFSET_MEMBER_UUID.getAttachmentKey();
    
    private transient Exception ex = null;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(profilePropName);
        out.writeObject(refsetUuidPropName);
        out.writeObject(refsetPositionSetPropName);
        out.writeObject(snomedPositionSetPropName);
        out.writeObject(conceptToReplaceUuidPropName);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion >= 1) {
            profilePropName = (String) in.readObject();
            refsetUuidPropName = (String) in.readObject();
            refsetPositionSetPropName = (String) in.readObject();
            snomedPositionSetPropName = (String) in.readObject();
            conceptToReplaceUuidPropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public String getProfilePropName() {
        return profilePropName;
    }

    public void setProfilePropName(String profilePropName) {
        this.profilePropName = profilePropName;
    }


    /**
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(final I_EncodeBusinessProcess process,
            final I_Work worker) throws TaskFailedException {
        try {
            ex = null;
            if (SwingUtilities.isEventDispatchThread()) {
                doRun(process, worker);
            } else {
                SwingUtilities.invokeAndWait(new Runnable() {

                    public void run() {
                        doRun(process, worker); 
                    }

                });
            }
        } catch (InterruptedException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } 
        if (ex != null) {
            throw new TaskFailedException(ex);
        }
        return Condition.CONTINUE;
    }

    private void doRun(final I_EncodeBusinessProcess process,
            final I_Work worker) {
        try {
            I_ConfigAceFrame config = (I_ConfigAceFrame) process.getProperty(getProfilePropName());
            ClearWorkflowDetailsSheet clear = new ClearWorkflowDetailsSheet();
            clear.setProfilePropName(getProfilePropName());
            clear.evaluate(process, worker);
            JPanel workflowDetailsSheet = config.getWorkflowDetailsSheet();
            int width = 750;
            int height = 150;
            workflowDetailsSheet.setSize(width, height);

            
           UUID refsetSpecUuid = (UUID) process.getProperty(refsetUuidPropName);
           Set<I_Position> refsetSpecVersionSet = (Set<I_Position>) process.getProperty(refsetPositionSetPropName);
           Set<I_Position> sourceTerminologyVersionSet = (Set<I_Position>) process.getProperty(snomedPositionSetPropName);
           UUID conceptUnderReviewUuid = (UUID) process.getProperty(conceptToReplaceUuidPropName);
           I_ConfigAceFrame frameConfig = (I_ConfigAceFrame) process.getProperty(getProfilePropName());
           UUID clauseToUpdate = (UUID) process.getProperty(clauseToUpdateMemberUuidPropName);
           
           // Block to facilitate testing...
           if (refsetSpecUuid == null) {
               refsetSpecUuid = RefsetAuxiliary.Concept.REFSET_IDENTITY.getUids().iterator().next();
           }
           if (refsetSpecVersionSet == null) {
               refsetSpecVersionSet = frameConfig.getViewPositionSet();
           }
           if (sourceTerminologyVersionSet == null) {
               sourceTerminologyVersionSet = frameConfig.getViewPositionSet();
           }
           if (conceptUnderReviewUuid == null) {
               // uuid for abnormal cortisol...
               conceptUnderReviewUuid = UUID.fromString("fdfb42fb-abe0-360a-bd8e-faae06d2dd06");
           }
           if (clauseToUpdate == null) {
               
           }

           I_GetConceptData refsetSpec = LocalVersionedTerminology.get().getConcept(refsetSpecUuid);
           I_GetConceptData conceptUnderReview = LocalVersionedTerminology.get().getConcept(conceptUnderReviewUuid);
            workflowDetailsSheet.add(new RefreshSpecClausePanel(refsetSpec,
                                                                refsetSpecVersionSet, 
                                                                sourceTerminologyVersionSet,
                                                                conceptUnderReview, 
                                                                clauseToUpdate,
                                                                frameConfig));
        } catch (Exception e) {
            ex = e;
        }
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        // Nothing to do

    }
    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }
    
    public String getRefsetUuidPropName() {
		return refsetUuidPropName;
	}

	public void setRefsetUuidPropName(String refsetUuidPropName) {
		this.refsetUuidPropName = refsetUuidPropName;
	}

	public String getRefsetPositionSetPropName() {
		return refsetPositionSetPropName;
	}

	public void setRefsetPositionSetPropName(String refsetPositionSetPropName) {
		this.refsetPositionSetPropName = refsetPositionSetPropName;
	}

	public String getSnomedPositionSetPropName() {
		return snomedPositionSetPropName;
	}

	public void setSnomedPositionSetPropName(String snomedPositionSetPropName) {
		this.snomedPositionSetPropName = snomedPositionSetPropName;
	}

	public String getConceptToReplaceUuidPropName() {
		return conceptToReplaceUuidPropName;
	}

	public void setConceptToReplaceUuidPropName(String conceptToReplaceUuidPropName) {
		this.conceptToReplaceUuidPropName = conceptToReplaceUuidPropName;
	}

	public String getClauseToUpdateMemberUuidPropName() {
		return clauseToUpdateMemberUuidPropName;
	}

	public void setClauseToUpdateMemberUuidPropName(
			String clauseToUpdateMemberUuidPropName) {
		this.clauseToUpdateMemberUuidPropName = clauseToUpdateMemberUuidPropName;
	}
}