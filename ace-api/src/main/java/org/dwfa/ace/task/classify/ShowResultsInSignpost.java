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
package org.dwfa.ace.task.classify;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/classify", type = BeanType.TASK_BEAN) })
public class ShowResultsInSignpost extends AbstractTask {

    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        final int objDataVersion = in.readInt();

        if (objDataVersion <= dataVersion) {

        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess arg0, I_Work arg1) throws TaskFailedException {
        // nothing to do...
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {

        try {
            final StringBuilder sb = new StringBuilder();
            sb.append("<tr><th>")
                .append("CONCEPT 1")
                .append("</th><th>")
                .append("RELATIONSHIPS")
                .append("</th><th>")
                .append("CONCEPT 2")
                .append("</th><th>")
                .append("GROUP")
                .append("</th></tr>\n");

            I_SnorocketFactory rocket = (I_SnorocketFactory) process.readAttachement(ProcessKey.SNOROCKET.getAttachmentKey());

            rocket.getResults(new ProcessResults(worker, sb));

            worker.getLogger().info(sb.toString());

            // update signpost
            I_ConfigAceFrame config = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
            final JPanel signpostPanel = config.getSignpostPanel();

            final JLabel resultsTable = new JLabel("<html><table align=\"center\" border=\"1\">" + sb
                + "</table></html>");
            final JComponent component = new JScrollPane(resultsTable);

            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                    Component[] components = signpostPanel.getComponents();
                    for (int i = 0; i < components.length; i++) {
                        signpostPanel.remove(components[i]);
                    }

                    signpostPanel.setLayout(new GridBagLayout());
                    GridBagConstraints c = new GridBagConstraints();
                    c.fill = GridBagConstraints.BOTH;
                    c.gridx = 0;
                    c.gridy = 0;
                    c.gridheight = 1;
                    c.weightx = 1.0;
                    c.weighty = 1.0;
                    c.anchor = GridBagConstraints.NORTHWEST;
                    signpostPanel.add(component, c);
                    signpostPanel.validate();
                    Container cont = signpostPanel;
                    while (cont != null) {
                        cont.validate();
                        cont = cont.getParent();
                    }
                }
            });

            worker.getLogger().info("Finished get results. ");
            // worker.getLogger().info(
            // "Stated and inferred: " + statedAndInferredCount
            // + " stated and subsumbed: "
            // + statedAndSubsumedCount + " inferred count: "
            // + inferredRelCount);

        } catch (Exception e) {
            throw new TaskFailedException(e);
        }

        return Condition.CONTINUE;
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    private class ProcessResults implements I_SnorocketFactory.I_Callback {

        final private I_Work worker;
        final private StringBuilder sb;

        private I_TermFactory termFactory = LocalVersionedTerminology.get();
        public I_GetConceptData relCharacteristic;
        public I_GetConceptData relRefinability;
        public I_GetConceptData relStatus;
        private int returnedRelCount = 0;

        public ProcessResults(final I_Work worker, StringBuilder sb) throws Exception {
            this.worker = worker;
            this.sb = sb;

            relCharacteristic = termFactory.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids());
            relRefinability = termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids());
            relStatus = termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids());

            worker.getLogger().info("Inferred id is " + relCharacteristic.getConceptId());
            worker.getLogger().info("Inferred UUIDs are " + relCharacteristic.getUids());
            worker.getLogger().info("Inferred concept is " + relCharacteristic);
        }

        public void addRelationship(int conceptId1, int roleId, int conceptId2, int group) {
            try {
                returnedRelCount++;
                final I_GetConceptData relSource = termFactory.getConcept(conceptId1);
                final I_GetConceptData relType = termFactory.getConcept(roleId);
                final I_GetConceptData relDestination = termFactory.getConcept(conceptId2);
                // worker.getLogger().info(relSource + " " + relType + " " +
                // relDestination + " " + group);

                sb.append("<tr><td>").append(relSource).append("</td><td>").append(relType).append("</td><td>").append(
                    relDestination).append("</td><td>").append(group).append("</td></tr>\n");
            } catch (TerminologyException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

}
