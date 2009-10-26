package org.dwfa.ace.task.refset.rfc;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
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
import org.dwfa.bpa.process.Priority;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.bpa.tasks.util.FileContent;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Gets the data from the Request for change panel and verifies that the
 * required data has been filled in.
 * 
 * @author Chrissy Hill
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec/wf", type = BeanType.TASK_BEAN) })
public class GetRequestForChangePanelDataTask extends AbstractTask {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;
    private String nextUserTermEntryPropName = ProcessAttachmentKeys.NEXT_USER.getAttachmentKey();
    private String commentsPropName = ProcessAttachmentKeys.MESSAGE.getAttachmentKey();
    private String refsetUuidPropName = ProcessAttachmentKeys.WORKING_REFSET.getAttachmentKey();
    private String originalRequestPropName = ProcessAttachmentKeys.SEARCH_ALL.getAttachmentKey();

    private I_TermFactory termFactory;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(nextUserTermEntryPropName);
        out.writeObject(commentsPropName);
        out.writeObject(refsetUuidPropName);
        out.writeObject(originalRequestPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            nextUserTermEntryPropName = (String) in.readObject();
            commentsPropName = (String) in.readObject();
            refsetUuidPropName = (String) in.readObject();
            originalRequestPropName = (String) in.readObject();
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
                if (RequestForChangePanel.class.isAssignableFrom(c.getClass())) {
                    RequestForChangePanel panel = (RequestForChangePanel) c;

                    I_GetConceptData refset = panel.getRefset();
                    I_GetConceptData editor = panel.getEditor(); // TODO check
                    String originalRequest = panel.getOriginalRequest();
                    String comments = panel.getComments();
                    Calendar deadline = panel.getDeadline();
                    String priority = panel.getPriority();
                    HashSet<File> attachments = panel.getAttachments();

                    Priority p;
                    if (priority == null) {
                        JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                            "You must select a priority. ", "", JOptionPane.ERROR_MESSAGE);
                        return Condition.ITEM_CANCELED;
                    }
                    if (priority.equals("Highest")) {
                        p = Priority.HIGHEST;
                    } else if (priority.equals("High")) {
                        p = Priority.HIGH;
                    } else if (priority.equals("Normal")) {
                        p = Priority.NORMAL;
                    } else if (priority.equals("Low")) {
                        p = Priority.LOW;
                    } else if (priority.equals("Lowest")) {
                        p = Priority.LOWEST;
                    } else {
                        p = null;
                    }

                    if (comments != null) {
                        process.setProperty(commentsPropName, comments);
                    } else {
                        process.setProperty(commentsPropName, "");
                    }

                    if (editor != null) {
                        RefsetSpecWizardTask wizard = new RefsetSpecWizardTask();
                        String inboxAddress = wizard.getInbox(editor);
                        process.setProperty(nextUserTermEntryPropName, inboxAddress);
                        process.setProperty(ProcessAttachmentKeys.EDITOR_UUID.getAttachmentKey(), new UUID[] { editor
                            .getUids().iterator().next() });
                        if (inboxAddress == null) {
                            JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                                "Request for change wizard cannot be completed. The selected editor has no assigned inbox : "
                                    + editor, "", JOptionPane.ERROR_MESSAGE);
                            return Condition.ITEM_CANCELED;
                        }
                    } else {
                        JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                            "You must select an editor. ", "", JOptionPane.ERROR_MESSAGE);
                        return Condition.ITEM_CANCELED;
                    }

                    if (originalRequest != null) {
                        process.setProperty(originalRequestPropName, originalRequest);
                    }

                    process.setOriginator(config.getUsername());

                    if (refset == null) {
                        JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), "You must select a refset. ",
                            "", JOptionPane.ERROR_MESSAGE);
                        return Condition.ITEM_CANCELED;
                    }
                    process.setSubject("Request for change : " + refset.getInitialText());
                    process.setName("Request for change : " + refset.getInitialText());
                    process.setPriority(p);

                    if (deadline == null) {
                        JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                            "You must select a deadline. ", "", JOptionPane.ERROR_MESSAGE);
                        return Condition.ITEM_CANCELED;
                    }
                    process.setDeadline(deadline.getTime());

                    process.setProperty(refsetUuidPropName, refset.getUids().iterator().next());
                    process.setProperty(ProcessAttachmentKeys.OWNER_UUID.getAttachmentKey(), new UUID[] { config
                        .getDbConfig().getUserConcept().getUids().iterator().next() });
                    process.setProperty(ProcessAttachmentKeys.EDITOR_UUID.getAttachmentKey(), new UUID[] { editor
                        .getUids().iterator().next() });
                    for (File file : attachments) {
                        process.writeAttachment(file.getName(), new FileContent(file));
                    }

                    return Condition.ITEM_COMPLETE;

                }
            }
            return Condition.ITEM_COMPLETE;
        } catch (Exception e) {
            e.printStackTrace();
            throw new TaskFailedException(e.getMessage());
        }
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.ITEM_CANCELED_OR_COMPLETE;
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

    public String getRefsetUuidPropName() {
        return refsetUuidPropName;
    }

    public void setRefsetUuidPropName(String refsetUuidPropName) {
        this.refsetUuidPropName = refsetUuidPropName;
    }

    public String getOriginalRequestPropName() {
        return originalRequestPropName;
    }

    public void setOriginalRequestPropName(String originalRequestPropName) {
        this.originalRequestPropName = originalRequestPropName;
    }

}
