package org.dwfa.ace.task.refset.spec.wf;

import java.awt.Component;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.UUID;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.refset.spec.RefsetSpecWizardTask;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.jini.TermEntry;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Select the next person the BP will go to. If there are no selected reviewers,
 * than this will automatically be the owner of the BP. Otherwise, the user is
 * prompted to select either the owner or one of the reviewers.
 * 
 * @author Chrissy Hill
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec/wf", type = BeanType.TASK_BEAN) })
public class GetSelectedOwnerOrReviewerTask extends AbstractTask {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;
    private String nextUserTermEntryPropName = ProcessAttachmentKeys.NEXT_USER.getAttachmentKey();
    private String commentsPropName = ProcessAttachmentKeys.MESSAGE.getAttachmentKey();

    private I_TermFactory termFactory;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(nextUserTermEntryPropName);
        out.writeObject(commentsPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            nextUserTermEntryPropName = (String) in.readObject();
            commentsPropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(final I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {

        try {
            termFactory = LocalVersionedTerminology.get();

            I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
            JPanel workflowDetailsSheet = config.getWorkflowDetailsSheet();
            for (Component c : workflowDetailsSheet.getComponents()) {
                if (SelectOwnerOrReviewerPanel.class.isAssignableFrom(c.getClass())) {
                    SelectOwnerOrReviewerPanel panel = (SelectOwnerOrReviewerPanel) c;

                    TermEntry selectedUser = panel.getSelectedUser();
                    String comments = panel.getComments();
                    if (comments != null) {
                        process.setProperty(commentsPropName, comments);
                    } else {
                        process.setProperty(commentsPropName, "");
                    }

                    RefsetSpecWizardTask wizard = new RefsetSpecWizardTask();
                    I_GetConceptData nextUserConcept = termFactory.getConcept(selectedUser.getIds());
                    I_GetConceptData ownerConcept =
                            termFactory.getConcept((UUID[]) process.readProperty(ProcessAttachmentKeys.OWNER_UUID
                                .getAttachmentKey()));

                    String inboxAddress = wizard.getInbox(nextUserConcept);
                    if (inboxAddress == null) {
                        JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                            "Refset wizard cannot be completed. The selected user has no assigned inbox.", "",
                            JOptionPane.ERROR_MESSAGE);
                        return Condition.ITEM_CANCELED;
                    }

                    process.setProperty(nextUserTermEntryPropName, inboxAddress);

                    process.setProperty(ProcessAttachmentKeys.REVIEWER_UUID.getAttachmentKey(),
                        new UUID[] { nextUserConcept.getUids().iterator().next() });

                    if (nextUserConcept.equals(ownerConcept)) {
                        return Condition.PREVIOUS;
                    } else {
                        return Condition.CONTINUE;
                    }
                }
            }
            return Condition.CONTINUE;
        } catch (Exception e) {
            e.printStackTrace();
            throw new TaskFailedException(e.getMessage());
        }
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.PREVIOUS_CONTINUE_CANCEL;
    }

    public String getNextUserTermEntryPropName() {
        return nextUserTermEntryPropName;
    }

    public void setNextUserTermEntryPropName(String nextUserTermEntryPropName) {
        this.nextUserTermEntryPropName = nextUserTermEntryPropName;
    }

    public String getCommentsPropName() {
        return commentsPropName;
    }

    public void setCommentsPropName(String commentsPropName) {
        this.commentsPropName = commentsPropName;
    }

}
