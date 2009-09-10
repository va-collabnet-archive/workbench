package org.dwfa.ace.task.refset.spec;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * The RefsetSpecWizardTask prompts the user to input required data to create a
 * new refset. This includes: refset name, additional comments, requestor,
 * deadline, priority, file attachments and a designated editor.
 * 
 * @author Chrissy Hill
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec", type = BeanType.TASK_BEAN) })
public class RefsetSpecWizardTask extends AbstractTask {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;

    private Condition condition;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(final I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {

        try {

            I_TermFactory termFactory = LocalVersionedTerminology.get();
            condition = Condition.ITEM_CANCELED;
            I_GetConceptData userParent = termFactory.getConcept(ArchitectonicAuxiliary.Concept.USER.getUids());
            I_IntSet allowedTypes = termFactory.newIntSet();
            allowedTypes.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()).getConceptId());

            // create list of editors -> FSN, for use in the drop down list
            final Set<I_GetConceptData> editors = userParent.getDestRelOrigins(allowedTypes, true, true);
            final HashMap<String, I_GetConceptData> editorNames = new HashMap<String, I_GetConceptData>();
            I_GetConceptData fsnConcept =
                    termFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());
            I_IntSet fsnAllowedTypes = termFactory.newIntSet();
            fsnAllowedTypes.add(fsnConcept.getConceptId());
            for (I_GetConceptData editor : editors) {
                String latestDescription = null;
                int latestVersion = Integer.MIN_VALUE;
                List<I_DescriptionTuple> descriptionResults =
                        editor.getDescriptionTuples(null, fsnAllowedTypes, null, true);

                for (I_DescriptionTuple descriptionTuple : descriptionResults) {
                    if (descriptionTuple.getVersion() > latestVersion) {
                        latestVersion = descriptionTuple.getVersion();
                        latestDescription = descriptionTuple.getText();
                    }
                }
                editorNames.put(latestDescription, editor);
            }

            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {

                    JFrame frame = new JFrame("New refset spec wizard");
                    NewRefsetSpecWizard wizard = new NewRefsetSpecWizard(frame);
                    wizard.getDialog().setTitle("New refset spec wizard");

                    NewRefsetSpecForm1 panel1 = new NewRefsetSpecForm1(wizard);
                    wizard.registerWizardPanel("panel1", panel1);

                    NewRefsetSpecForm2 panel2 = new NewRefsetSpecForm2(editorNames.keySet());
                    wizard.registerWizardPanel("panel2", panel2);

                    wizard.setCurrentPanel("panel1");
                    frame.setLocationRelativeTo(null); // center frame

                    wizard.showModalDialog();
                    boolean createData = wizard.isCreateData();
                    if (createData) {

                        String refsetName = panel1.getRefsetNameTextField();
                        String refsetReqs = panel1.getRefsetRequirementsTextField();
                        HashSet<File> attachments = panel1.getAttachments();

                        String requester = panel2.getRequestor();
                        I_GetConceptData editor = editorNames.get(panel2.getSelectedEditor());
                        Calendar deadline = panel2.getDeadline();
                        System.out.println(deadline);
                        String priority = panel2.getPriority();

                        // TODO process subject -> refset_name + " " +
                        // "creation request"
                        // TODO process name -> "create refset"
                        // TODO originator -> whoever executed BP
                        // TODO destination -> selected editor's inbox using
                        // "user inbox" description type

                        try {
                            process.setProperty(ProcessAttachmentKeys.MESSAGE.getAttachmentKey(), refsetName);
                            process.setProperty(ProcessAttachmentKeys.ASSIGNEE.getAttachmentKey(), editor
                                .getInitialText());
                            process.setProperty("A: CONCEPT_NAME", refsetName);
                            RefsetSpecWizardTask.this.setCondition(Condition.ITEM_COMPLETE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            return getCondition();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TaskFailedException(ex);
        }
    }

    public void setCondition(Condition c) {
        condition = c;
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.ITEM_CANCELED_OR_COMPLETE;
    }

    public Condition getCondition() {
        return condition;
    }
}
