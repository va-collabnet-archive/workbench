/**
 * Copyright (c) 2009 International Health Terminology Standards Development Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.batch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.batch.BatchActionEvent.BatchActionEventType;
import static org.ihtsdo.batch.BatchActionTask.nidToName;
import org.ihtsdo.tk.api.AnalogBI;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;

/**
 * BatchActionTaskDescriptionRetire
 *
 */
public class BatchActionTaskDescriptionRetire
        extends AbstractBatchActionTaskDescription {

    public BatchActionTaskDescriptionRetire() {
        super();
    }

    // BatchActionTask
    @Override
    public boolean execute(ConceptVersionBI c, EditCoordinate ec, ViewCoordinate vc)
            throws IOException, InvalidCAB, ContradictionException {
        boolean changed = false;
        Collection<? extends DescriptionChronicleBI> descriptions = c.getDescriptions();
        ArrayList<DescriptionChronicleBI> descList = new ArrayList<>();
        for (DescriptionChronicleBI dcbi : descriptions) {
            boolean criteriaPass;
            DescriptionVersionBI dvbi = null;
            try {
                dvbi = dcbi.getVersion(vc);
            } catch (ContradictionException ex) {
                BatchActionEventReporter.add(new BatchActionEvent(c,
                        BatchActionTaskType.DESCRIPTION_RETIRE,
                        BatchActionEventType.EVENT_ERROR,
                        "ERROR: multiple active versions"));
            }

            if (dvbi == null) {
                continue; // nothing to change
            }

            criteriaPass = testCriteria(dvbi, vc);

            if (criteriaPass) {
                changed = true;
                ComponentVersionBI analog;
                for (int editPath : ec.getEditPaths()) {
                    analog = (ComponentVersionBI) dvbi.makeAnalog(
                            RETIRED_NID,
                            Long.MAX_VALUE,
                            ec.getAuthorNid(),
                            ec.getModuleNid(),
                            editPath);
                }

                retireFromRefexes(dvbi);

                BatchActionEventReporter.add(new BatchActionEvent(c,
                        BatchActionTaskType.DESCRIPTION_RETIRE,
                        BatchActionEventType.EVENT_SUCCESS,
                        "description retired: " + dvbi.getText()));
            }
        }

        if (!changed) {
            BatchActionEventReporter.add(new BatchActionEvent(c,
                    BatchActionTaskType.DESCRIPTION_RETIRE,
                    BatchActionEventType.EVENT_NOOP,
                    "description not retired : "
                    + nidToName(c.getConceptNid())));
        }

        return changed;
    }

    private void retireFromRefexes(ComponentVersionBI component) {
        DescriptionVersionBI desc = (DescriptionVersionBI) component;
        try {
            I_AmPart componentVersion;
            ViewCoordinate vc = config.getViewCoordinate();
            Collection<? extends RefexChronicleBI> refexes = desc.getRefexesActive(vc);
            for (RefexChronicleBI refex : refexes) {
                int refexNid = refex.getRefexNid();
                componentVersion = (I_AmPart) refex;
                for (PathBI ep : config.getEditingPathSet()) {
                    componentVersion.makeAnalog(
                            SnomedMetadataRfx.getSTATUS_RETIRED_NID(),
                            Long.MAX_VALUE,
                            config.getEditCoordinate().getAuthorNid(),
                            config.getEditCoordinate().getModuleNid(),
                            ep.getConceptNid());
                }
                I_GetConceptData refexConcept = Terms.get().getConceptForNid(refexNid);
                if (refexConcept.isAnnotationStyleRefex()) {
                    // Ts.get().addUncommitted(c); <-- done in BatchActionProcessor for concept
                } else {
                    ts.addUncommitted(refexConcept);
                }
            }
        } catch (IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
    }

}
