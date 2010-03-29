/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dwfa.ace.task.data.checks;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.refset.ClosestDistanceHashSet;
import org.dwfa.ace.refset.ConceptRefsetInclusionDetails;
import org.dwfa.ace.refset.MemberRefsetCalculator;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

// TODO: write java docs.
/**
 * <h1>VerifyNoRefSetConflicts</h1> <br>
 * <p>
 * The <code>VerifyNoRefSetConflicts</code> class checks for conflicts between
 * concepts in the hierarchy.
 * </P>
 * 
 * <br>
 * <br>
 * 
 * @see <code>org.dwfa.bpa.tasks.AbstractTask</code>
 * @author PeterVawser
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/data checks", type = BeanType.TASK_BEAN) })
public class VerifyNoRefSetConflicts extends AbstractTask {
    /*
     * Priavte instance variables
     */
    private static final long serialVersionUID = 1;
    private static final int dataVersion = 2;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }// End method writeObject

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
            // Do nothing...
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }// End method readObject

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
        try {

            Condition conflicts = Condition.FALSE;

            MyMemberRefsetCalculator mrc = new MyMemberRefsetCalculator();
            mrc.setOutputDirectory(new File("."));
            mrc.setValidateOnly(true);
            mrc.run();

            I_ConfigAceFrame config = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
            final JPanel signpostPanel = config.getSignpostPanel();
            final JPanel p = new JPanel();
            final JScrollPane jsp = new JScrollPane(p);
            final List<String> details = mrc.conflictDetails;

            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                    Component[] components = signpostPanel.getComponents();
                    for (int i = 0; i < components.length; i++) {
                        signpostPanel.remove(components[i]);
                    }

                    signpostPanel.setLayout(new GridBagLayout());

                    p.setLayout(new GridBagLayout());
                    p.setBackground(Color.WHITE);

                    GridBagConstraints c = new GridBagConstraints();
                    c.fill = GridBagConstraints.BOTH;
                    c.gridx = 0;
                    c.gridy = 0;
                    c.gridheight = 5;
                    c.weightx = 1.0;
                    c.weighty = 1.0;
                    c.anchor = GridBagConstraints.NORTHWEST;

                    StringBuffer sb = new StringBuffer();
                    sb.append("<html><body><tr><td>");
                    int counter = 0;
                    for (String conflict : details) {
                        // Add to signpost
                        if (counter == 0) {
                            sb.append("<h1>" + conflict + "</h1><ul>");
                        } else {
                            sb.append("<li>" + conflict + "</li>");
                        }

                        counter++;
                    }

                    if (details.size() > 0) {
                        sb.append("</ul>");
                    }

                    sb.append("</td></tr></body></html>");

                    p.add(new JLabel(sb.toString()), c);
                    signpostPanel.add(jsp, c);
                    signpostPanel.validate();
                    Container cont = signpostPanel;

                    while (cont != null) {
                        cont.validate();
                        cont = cont.getParent();
                    }
                }
            });

            return mrc.hasConflicts();
        } catch (InterruptedException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        }

    }// End method evaluate

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do

    }// End method complete

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return AbstractTask.CONDITIONAL_TEST_CONDITIONS;
    }// End method getConditions

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
     */
    public int[] getDataContainerIds() {
        return new int[] {};
    }// End getDataContainerIds

    class MyMemberRefsetCalculator extends MemberRefsetCalculator {

        private Condition conflicts = Condition.FALSE;
        protected List<String> conflictDetails = new ArrayList<String>();

        public Condition hasConflicts() {
            return conflicts;
        }

        protected void setMembers() throws Exception {

            for (Integer refset : newRefsetMembers.keySet()) {
                ClosestDistanceHashSet exclusions = new ClosestDistanceHashSet();

                conflictDetails.add("Conflicts in refset " + termFactory.getConcept(refset) + " are: ");

                ClosestDistanceHashSet newMembers = newRefsetMembers.get(refset);
                ClosestDistanceHashSet oldMembers = newRefsetExclusion.get(refset);
                if (newMembers != null) {

                    for (ConceptRefsetInclusionDetails i : newMembers.values()) {
                        if (oldMembers != null && oldMembers.containsKey(i.getConceptId())) {
                            List<Integer> addedConcepts = new ArrayList<Integer>();
                            for (ConceptRefsetInclusionDetails old : oldMembers.values()) {
                                // Show only first level conflict
                                I_IntSet isARel = termFactory.newIntSet();
                                isARel.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));
                                isARel.add(termFactory.uuidToNative(SNOMED.Concept.IS_A.getUids()));
                                if (old.equals(i)) {
                                    for (I_GetConceptData c : termFactory.getConcept(i.getConceptId())
                                        .getSourceRelTargets(null, isARel, null, false)) {
                                        int conceptId = c.getConceptId();
                                        if (conceptId == termFactory.getConcept(i.getInclusionReasonId())
                                            .getConceptId()
                                            || conceptId == termFactory.getConcept(old.getInclusionReasonId())
                                                .getConceptId()) {

                                            if (!addedConcepts.contains(new Integer(conceptId))) {

                                                StringBuffer sb = new StringBuffer();
                                                sb.append(termFactory.getConcept(i.getConceptId()).toString());
                                                sb.append(" because of "
                                                    + termFactory.getConcept(i.getInclusionReasonId()).toString());
                                                sb.append(" conflicts with "
                                                    + termFactory.getConcept(old.getInclusionReasonId()).toString());

                                                conflictDetails.add(sb.toString());
                                                addedConcepts.add(new Integer(conceptId));
                                            }
                                        }
                                    }
                                }
                                // Show all levels conflicts
                                // if (old.equals(i)) {
                                // StringBuffer sb = new StringBuffer();
                                // sb.append(termFactory.getConcept(i.getConceptId()).toString());
                                // sb.append(" because of " +
                                // termFactory.getConcept(i.getInclusionReasonId()).toString());
                                // sb.append(" conflicts with "
                                // +termFactory.getConcept(old.getInclusionReasonId()).toString());
                                //									
                                // conflictDetails.add(sb.toString());
                                //									
                                // }
                            }
                            conflicts = Condition.TRUE;
                        }
                    }
                }
            }
        }
    }// End nested class MyMemberRefsetCalculator

}// End class VerifyNoRefSetConflicts
