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
package org.dwfa.ace.task.refset.spec.compute;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.WorkerAttachmentKeys;
//import org.dwfa.ace.task.refset.spec.RefsetSpec;
import org.dwfa.app.DwfaEnv;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.cs.ChangeSetPolicy;
import org.ihtsdo.tk.query.RefsetSpec;
import org.ihtsdo.tk.query.ComputeFromSpec;
import org.ihtsdo.tk.query.RefsetSpecFactory;
import org.ihtsdo.tk.query.RefsetSpecQuery;
import org.ihtsdo.tk.query.RefsetComputer.ComputeType;

/**
 * Computes the members of a refset given a refset spec. This refset spec is the
 * one currently displayed in the refset spec editing panel. The refset spec's
 * "specifies refset" relationship indicates which member refset will be
 * created.
 * 
 * @author Chrissy Hill
 * 
 */
@BeanList(specs = {
    @Spec(directory = "tasks/refset/spec", type = BeanType.TASK_BEAN)})
public class ComputeRefsetFromSpecTask extends AbstractTask {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;
    private boolean cancelComputation = false;
    private Set<Integer> nestedRefsets;
    private Set<Integer> excludedRefsets;
    private ViewCoordinate vc;
    private EditCoordinate ec;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            // Nothing to do
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    public Condition computeRefset(I_ConfigAceFrame configFrame, ConceptChronicleBI refset, boolean showActivityPanel) {
        this.ec = configFrame.getEditCoordinate();
        this.vc = configFrame.getViewCoordinate();
        if (refset == null) {
            AceLog.getAppLog().info("No refset in refset spec panel to compute.");
            if (!DwfaEnv.isHeadless()) {
                JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                        "No refset in refset spec panel to compute.", "", JOptionPane.ERROR_MESSAGE);
            }
            return Condition.ITEM_CANCELED;
        }
        
        try {
            ChangeSetPolicy csPolicy = configFrame.getDbConfig().getRefsetChangesChangeSetPolicy();
            RefsetSpec refsetSpecHelper = new RefsetSpec(refset, true, vc);
            ConceptChronicleBI refsetSpec = refsetSpecHelper.getRefsetSpecConcept();
            AceLog.getAppLog().info("Refset: " + refset.toUserString() + " " + refset.getPrimUuid());
            if (refsetSpec == null) {
                AceLog.getAppLog().info("Refset not a spec refset: " + refset.toUserString());
                if (!refset.getRefsetMembers().isEmpty()) {
                    AceLog.getAppLog().info("Refset has members. Will compute marked parents for members.");
                    ComputeFromSpec.computeRefset(null, vc, ec, refset.getNid(), csPolicy);
                }
                return Condition.ITEM_COMPLETE;
            }else{
                AceLog.getAppLog().info("Refset spec: " + refsetSpec.toUserString()+ " " + refsetSpec.getPrimUuid());
            }
            ComputeType computeType = ComputeType.CONCEPT; // default
            if (refsetSpecHelper.isDescriptionComputeType()) {
                computeType = ComputeType.DESCRIPTION;
            } else if (refsetSpecHelper.isRelationshipComputeType()) {
                AceLog.getAppLog().info("Invalid refset spec to compute - relationship compute types not supported.");
                if (!DwfaEnv.isHeadless()) {
                    JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                            "Invalid refset spec to compute - relationship compute types not supported.", "",
                            JOptionPane.ERROR_MESSAGE);
                }
                return Condition.ITEM_CANCELED;
            }

            // verify a valid refset spec construction
            if (refsetSpec == null) {
                AceLog.getAppLog().info("Nested refset not a spec refset: " + refset.toUserString());
                return Condition.ITEM_COMPLETE;
            }
            // Step 1: create the query object, based on the refset spec
            RefsetSpecQuery query =
                    RefsetSpecFactory.createQuery(vc, refsetSpec, refset, computeType);

            // check validity of query
            if (!query.isValidQuery() && query.getTotalStatementCount() != 0) {
                getLogger().info("Refset spec has dangling AND/OR. These must have sub-statements.");
                if (!DwfaEnv.isHeadless()) {
                    JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                            "Refset spec has dangling AND/OR. These must have sub-statements.", "", JOptionPane.ERROR_MESSAGE);
                }
            }

            computeNestedRefsets(configFrame, showActivityPanel, query);

            AceLog.getAppLog().info("Start execution of refset spec : " + refsetSpec.toUserString());
            
            Condition condition = ComputeFromSpec.computeRefset(query, vc, ec, refset.getNid(), csPolicy);
            if (!DwfaEnv.isHeadless()) {
                Terms.get().getActiveAceFrameConfig().refreshRefsetTab();
            }

            if (cancelComputation || condition == Condition.ITEM_CANCELED) {
                Terms.get().cancel();
                getLogger().info("Refset compute cancelled.");
                if (!DwfaEnv.isHeadless()) {
                    JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), "Refset compute cancelled.", "",
                            JOptionPane.ERROR_MESSAGE);
                }
                return Condition.ITEM_CANCELED;
            }

            if (!DwfaEnv.isHeadless()) {
                JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), "Refset compute complete. Refset: " + refset.toUserString(), "",
                        JOptionPane.INFORMATION_MESSAGE);
            }
            return Condition.ITEM_COMPLETE;

        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
            try {
                Terms.get().cancel();
                Terms.get().getActiveAceFrameConfig().setCommitAbortButtonsVisible(true);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } catch (TerminologyException termException) {
                termException.printStackTrace();
            }
            return Condition.ITEM_CANCELED;
        }
    }
    
    private void computeNestedRefsets(I_ConfigAceFrame configFrame,boolean showActivityPanel, RefsetSpecQuery query)
            throws TerminologyException, IOException, Exception {
        // compute any nested refsets (e.g. if this spec uses
        // "Concept is member of : refset2", then the members of
        // "Refset2" will be calculated as part of the computation
        Set<Integer> nestedRefsets = query.getNestedRefsets();
        this.setNestedRefsets(nestedRefsets);
        for (Integer nestedRefsetId : nestedRefsets) {
            if (excludedRefsets == null || !excludedRefsets.contains(nestedRefsetId)) {

                Condition condition = computeRefset(configFrame, Ts.get().getConcept(nestedRefsetId), showActivityPanel);
                if (condition == Condition.ITEM_CANCELED) {
                    if (!DwfaEnv.isHeadless()) {
                        JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                                "Error computing dependant refset: " + Ts.get().getConcept(nestedRefsetId).toUserString()
                                + ". Re-run separately.", "", JOptionPane.ERROR_MESSAGE);
                    }
                    getNestedRefsets().addAll(nestedRefsets);
                }
                Terms.get().commit();
            }
        }
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        I_ConfigAceFrame configFrame =
                (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
        this.ec = configFrame.getEditCoordinate();
        this.vc = configFrame.getViewCoordinate();
        assert configFrame != null;
        try {
            ConceptChronicleBI refset = configFrame.getRefsetInSpecEditor();
            boolean showActivityPanel = true;
            excludedRefsets = new HashSet<Integer>(); // no excluded refsets when running as part of a task
            nestedRefsets = new HashSet<Integer>();
            Condition condition = computeRefset(configFrame, refset, showActivityPanel);
            configFrame.setCommitAbortButtonsVisible(true);
            return condition;
        } catch (Exception ex) {
            ex.printStackTrace();
            if (!DwfaEnv.isHeadless()) {
                JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), "Unable to complete refset compute: "
                        + ex.getMessage(), "", JOptionPane.ERROR_MESSAGE);
            }
            try {
                Terms.get().cancel();
                configFrame.setCommitAbortButtonsVisible(true);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            return Condition.ITEM_CANCELED;
        }
    }

    public int[] getDataContainerIds() {
        return new int[]{};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.ITEM_CANCELED_OR_COMPLETE;
    }

    public boolean isCancelComputation() {
        return cancelComputation;
    }

    public void setCancelComputation(boolean cancelComputation) {
        this.cancelComputation = cancelComputation;
    }

    public Set<Integer> getNestedRefsets() {
        return nestedRefsets;
    }

    public void setNestedRefsets(Set<Integer> nestedRefsets) {
        this.nestedRefsets = nestedRefsets;
    }

    public Set<Integer> getExcludedRefsets() {
        return excludedRefsets;
    }

    public void setExcludedRefsets(Set<Integer> excludedRefsets) {
        this.excludedRefsets = excludedRefsets;
    }
}
