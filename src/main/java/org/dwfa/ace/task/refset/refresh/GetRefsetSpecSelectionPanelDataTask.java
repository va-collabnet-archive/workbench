package org.dwfa.ace.task.refset.refresh;


import java.awt.Component;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Gets the data from the Refset Spec And Position panel and verifies that the
 * required data has been filled in.
 * 
 * @author Perry Reid
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec/wf", type = BeanType.TASK_BEAN) })
public class GetRefsetSpecSelectionPanelDataTask extends AbstractTask {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 2;
    private String refsetUuidPropName = ProcessAttachmentKeys.WORKING_REFSET.getAttachmentKey();

    private I_TermFactory termFactory;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(refsetUuidPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
            if (objDataVersion == 1) {
                // Read version 1 data fields...
            	String objCommentsPropName = (String) in.readObject();
                refsetUuidPropName = (String) in.readObject();
            } else if (objDataVersion == 2) {
                // Read version 2 data fields...
                 refsetUuidPropName = (String) in.readObject();
            } else {
            	// Initialize transient properties...
            }
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
                if (PanelRefsetSpecSelection.class.isAssignableFrom(c.getClass())) {
                	PanelRefsetSpecSelection panel = (PanelRefsetSpecSelection) c;

                    I_GetConceptData refset = panel.getRefset();
//                    String comments = panel.getComments();
                    I_GetConceptData owner = config.getDbConfig().getUserConcept();


//                    if (comments != null) {
//                        process.setProperty(commentsPropName, comments);
//                    } else {
//                        process.setProperty(commentsPropName, "");
//                    }


                    if (refset == null) {
                        JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), "You must select a refset. ",
                            "", JOptionPane.ERROR_MESSAGE);
                        return Condition.ITEM_CANCELED;
                    }
//                    process.setSubject("Request for change : " + refset.getInitialText());
//                    process.setName("Request for change : " + refset.getInitialText());
//                    process.setPriority(p);

                    process.setProperty(refsetUuidPropName, refset.getUids().iterator().next());

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

//    public String getCommentsPropName() {
//        return commentsPropName;
//    }
//
//    public void setCommentsPropName(String commentsPropName) {
//        this.commentsPropName = commentsPropName;
//    }

    public String getRefsetUuidPropName() {
        return refsetUuidPropName;
    }

    public void setRefsetUuidPropName(String refsetUuidPropName) {
        this.refsetUuidPropName = refsetUuidPropName;
    }

}
