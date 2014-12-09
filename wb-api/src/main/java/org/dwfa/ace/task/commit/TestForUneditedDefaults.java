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
package org.dwfa.ace.task.commit;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.Terms;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_float.RefexNidFloatVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

@BeanList(specs = { @Spec(directory = "tasks/ide/commit", type = BeanType.TASK_BEAN),
                   @Spec(directory = "plugins/precommit", type = BeanType.TASK_BEAN),
                   @Spec(directory = "plugins/commit", type = BeanType.TASK_BEAN) })
public class TestForUneditedDefaults extends AbstractConceptTest {

    private static final long serialVersionUID = 1;
    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            //
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    @Override
    public List<AlertToDataConstraintFailure> test(I_GetConceptData concept, boolean forCommit)
            throws TaskFailedException {
        List<AlertToDataConstraintFailure> alerts = new ArrayList<AlertToDataConstraintFailure>();
        try {
            for (I_DescriptionVersioned desc : concept.getDescs()) {
                alerts.addAll(testDescription(concept, desc, forCommit));
            }
            for(RelationshipChronicleBI rel : concept.getRelationshipsOutgoing()){
                alerts.addAll(testNumberRefex(concept, rel, forCommit));
            }
        } catch (IOException e) {
            throw new TaskFailedException(e);
        } catch (TerminologyException ex) {
           throw new TaskFailedException(ex);
        } catch (ContradictionException ex) {
           throw new TaskFailedException(ex);
        }
        return alerts;
    }

    private List<AlertToDataConstraintFailure> testDescription(I_GetConceptData concept, I_DescriptionVersioned<?> desc,
            boolean forCommit) {
        for (I_DescriptionPart part : desc.getMutableParts()) {
            if (part.getVersion() == Integer.MAX_VALUE) {
                if (part.getText().toLowerCase().startsWith("clone of ")) {
                    String alertString = "<html>Unedited default found:<br> <font color='blue'>" + part.getText()
                        + "</font><br>Please edit this value before commit...";
                    AlertToDataConstraintFailure.ALERT_TYPE alertType = AlertToDataConstraintFailure.ALERT_TYPE.WARNING;
                    if (forCommit) {
                        alertType = AlertToDataConstraintFailure.ALERT_TYPE.ERROR;
                    }
                    AlertToDataConstraintFailure alert = new AlertToDataConstraintFailure(alertType, alertString,
                        concept);

                    ArrayList<AlertToDataConstraintFailure> alertList = new ArrayList<AlertToDataConstraintFailure>();
                    alertList.add(alert);
                    return alertList;
                }
            }
        }
        return new ArrayList<AlertToDataConstraintFailure>();
    }
    
    private List<AlertToDataConstraintFailure> testNumberRefex(I_GetConceptData concept, RelationshipChronicleBI rel,
            boolean forCommit) throws TerminologyException, IOException, ContradictionException {
        ViewCoordinate viewCoordinate = Terms.get().getActiveAceFrameConfig().getViewCoordinate();
        RelationshipVersionBI rv = rel.getVersion(viewCoordinate);
        if (rv != null) {
            for (RefexVersionBI r : rv.getActiveAnnotations(viewCoordinate)) {
                if (RefexNidFloatVersionBI.class.isAssignableFrom(r.getClass())) {
                    RefexNidFloatVersionBI refex = (RefexNidFloatVersionBI) r;
                    if (refex.isUncommitted()) {
                        if (refex.getFloat1() <= 0) {
                            String alertString = "<html>Default value found:<br> <font color='blue'>" + refex.getFloat1()
                                    + "</font><br>Please edit this value before commit...";
                            AlertToDataConstraintFailure.ALERT_TYPE alertType = AlertToDataConstraintFailure.ALERT_TYPE.WARNING;
                            if (forCommit) {
                                alertType = AlertToDataConstraintFailure.ALERT_TYPE.ERROR;
                            }
                            AlertToDataConstraintFailure alert = new AlertToDataConstraintFailure(alertType, alertString,
                                    concept);

                            ArrayList<AlertToDataConstraintFailure> alertList = new ArrayList<AlertToDataConstraintFailure>();
                            alertList.add(alert);
                            return alertList;
                        }
                    }
                }
            }
        }
        return new ArrayList<AlertToDataConstraintFailure>();
    }

}
