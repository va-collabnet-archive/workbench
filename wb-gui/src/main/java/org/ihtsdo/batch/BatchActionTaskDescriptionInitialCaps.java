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

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Collection;
import org.ihtsdo.batch.BatchActionEvent.BatchActionEventType;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionAnalogBI;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;

/**
 * BatchActionTaskDescriptionInitialCaps
 *
 */
public class BatchActionTaskDescriptionInitialCaps
        extends AbstractBatchActionTaskDescription {

    boolean targetCaseSensitivity;

    public BatchActionTaskDescriptionInitialCaps() {
        super();
        targetCaseSensitivity = false;
    }

    public void setTargetCaseSensitivity(boolean cs) {
        this.targetCaseSensitivity = cs;
    }

    // BatchActionTask
    @Override
    public boolean execute(ConceptVersionBI c, EditCoordinate ec, ViewCoordinate vc)
            throws IOException, InvalidCAB, ContradictionException {

        boolean changed = false;
        Collection<? extends DescriptionChronicleBI> descriptions = c.getDescriptions();
        for (DescriptionChronicleBI dcbi : descriptions) {
            boolean criteriaPass;
            DescriptionVersionBI dvbi = null;
            try {
                dvbi = dcbi.getVersion(vc);
            } catch (ContradictionException ex) {
                BatchActionEventReporter.add(new BatchActionEvent(c,
                        BatchActionTaskType.DESCRIPTION_INITIAL_CHAR_CASE_SENSITIVITY,
                        BatchActionEventType.EVENT_ERROR,
                        "ERROR: multiple active versions"));
            }

            if (dvbi == null) {
                continue; // nothing to change
            }

            criteriaPass = testCriteria(dvbi, vc);

            // DO EDIT
            if (criteriaPass) {
                boolean currentCaseSensitivity = dvbi.isInitialCaseSignificant();
                if (currentCaseSensitivity != targetCaseSensitivity) {
                    changed = true;

                    DescriptionAnalogBI analog;
                    for (int editPath : ec.getEditPaths()) {
                        // similar approach as arena SetICSignificantAction
                        analog = (DescriptionAnalogBI) dvbi.makeAnalog(
                                CURRENT_NID,
                                Long.MAX_VALUE,
                                ec.getAuthorNid(),
                                ec.getModuleNid(),
                                editPath);
                        try {
                            analog.setInitialCaseSignificant(targetCaseSensitivity);
                        } catch (PropertyVetoException ex) {
                            BatchActionEventReporter.add(new BatchActionEvent(c,
                                    BatchActionTaskType.DESCRIPTION_INITIAL_CHAR_CASE_SENSITIVITY,
                                    BatchActionEventType.EVENT_ERROR,
                                    "description initial char sensitivity not updated: " + dvbi.getText()));
                            return false;
                        }
                    }

                    BatchActionEventReporter.add(new BatchActionEvent(c,
                            BatchActionTaskType.DESCRIPTION_INITIAL_CHAR_CASE_SENSITIVITY,
                            BatchActionEventType.EVENT_SUCCESS,
                            "description initial char sensitivity updated: " + dvbi.getText()));
                }
            }
        }

        if (!changed) {
            BatchActionEventReporter.add(new BatchActionEvent(c,
                    BatchActionTaskType.DESCRIPTION_INITIAL_CHAR_CASE_SENSITIVITY,
                    BatchActionEventType.EVENT_NOOP,
                    "description initial caps not changed on concept : "
                    + nidToName(c.getConceptNid())));
        }

        return changed;
    }

}
