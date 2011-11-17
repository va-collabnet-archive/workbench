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
import java.util.Collection;
import java.util.List;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.commit.failureconstraintfactory.AlertToDataConstraintFailureFactory;
import org.dwfa.ace.task.commit.failureconstraintfactory.SimpleConstraintFailureChooser;
import org.dwfa.ace.task.commit.validator.ConceptDescriptionFacade;
import org.dwfa.ace.task.commit.validator.GetConceptDataValidationStrategy;
import org.dwfa.ace.task.commit.validator.ValidationException;
import org.dwfa.ace.task.commit.validator.impl.NotEmptyConceptDataValidator;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.spec.ConceptSpec;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid.RefexCnidVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;

/**
 * The <code>TestForPreferredTermValue</code> class represents a data constraint
 * test that is to be run on any <code>concept</code> to verify that it has a
 * 'preferred term' that is not null and a value with a length greater
 * than <code>0</code>;
 * 
 * @author Matthew Edwards
 */
@BeanList(specs = {
    @Spec(directory = "tasks/ide/commit", type = BeanType.TASK_BEAN),
    @Spec(directory = "plugins/precommit", type = BeanType.TASK_BEAN),
    @Spec(directory = "plugins/commit", type = BeanType.TASK_BEAN)})
public class TestForPreferredTermValue extends AbstractConceptTest {

    private static final long serialVersionUID = 1;
    private static final int DATA_VERSION = 1;
    private static final String ALERT_MESSAGE = "<html>Empty value found:<br><font color='blue'>%1$s</font><br>Please enter a value before commit...";

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(DATA_VERSION);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion != DATA_VERSION) {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    @Override
    public List<AlertToDataConstraintFailure> test(I_GetConceptData concept, boolean forCommit)
            throws TaskFailedException {
        try {
            I_TermFactory termFactory = getTermFactory();

            List<AlertToDataConstraintFailure> alerts = new ArrayList<AlertToDataConstraintFailure>();

            I_GetConceptData requiredConcept = termFactory.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids());

            List<I_DescriptionVersioned> descriptions = new ConceptDescriptionFacade(termFactory, this).getAllDescriptions(concept);

            GetConceptDataValidationStrategy validator = new NotEmptyConceptDataValidator(requiredConcept,
                    descriptions, concept);
            try {
                validator.validate();
            } catch (ValidationException e) {
                getLogger().info(e.getMessage());
                alerts.add(getAlertFactory(forCommit).createAlertToDataConstraintFailure(
                        String.format(ALERT_MESSAGE, requiredConcept.toString()), concept));
            }
            int gbPrefCount = 0;
            int usPrefCount = 0;
            ConceptSpec EN_GB_LANG =
                    new ConceptSpec("GB English Dialect Subset",
                    UUID.fromString("a0982f18-ec51-56d2-a8b1-6ff8964813dd"));

            ConceptSpec EN_US_LANG =
                    new ConceptSpec("US English Dialect Subset",
                    UUID.fromString("29bf812c-7a77-595d-8b12-ea37c473a5e6"));
            ArrayList<I_DescriptionVersioned> uniqueDescriptions = new ArrayList<I_DescriptionVersioned>();
            for(I_DescriptionVersioned d : descriptions){
                if(!uniqueDescriptions.contains(d)){
                    uniqueDescriptions.add(d);
                }
            }
            for (I_DescriptionVersioned desc : uniqueDescriptions) {
                if (desc.getStatusNid() == SnomedMetadataRfx.getSTATUS_CURRENT_NID()
                        && desc.getTypeNid() == SnomedMetadataRfx.getDES_SYNONYM_NID()) {
                    if(this.isPreferredTerm(desc, EN_US_LANG)){
                        usPrefCount++;
                    }else if (this.isPreferredTerm(desc, EN_GB_LANG)){
                        gbPrefCount++;
                    }
                }
            }
            if (gbPrefCount > 1 || usPrefCount > 1) {
                alerts.add(getAlertFactory(forCommit).createAlertToDataConstraintFailure(
                        String.format("<html>More than one PT for dialect in concept",
                        requiredConcept.toString()), concept));
            }
            // return alerts;
            return alerts;
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    private AlertToDataConstraintFailureFactory getAlertFactory(final boolean forCommit) {
        return new SimpleConstraintFailureChooser(forCommit).getFactory();
    }

    private I_TermFactory getTermFactory() {
        return Terms.get();
    }

    private boolean isPreferredTerm(I_DescriptionVersioned desc, ConceptSpec evalRefset) {
        boolean isPreferredTerm = false;
        try {
            Collection<? extends RefexChronicleBI> refexes =
                    desc.getCurrentRefexes(Terms.get().getActiveAceFrameConfig().getViewCoordinate());
            int evalRefsetNid = Ts.get().getNidForUuids(evalRefset.getUuids());

            if (refexes != null) {
                for (RefexChronicleBI refex : refexes) {
                    if (refex.getCollectionNid() == evalRefsetNid) {
                        if (RefexVersionBI.class.isAssignableFrom(refex.getClass())) {
                            RefexVersionBI<?> rv = (RefexVersionBI<?>) refex;

                            if (RefexCnidVersionBI.class.isAssignableFrom(rv.getClass())) {
                                int cnid = ((RefexCnidVersionBI) rv).getCnid1();
                                if (cnid == SnomedMetadataRfx.getDESC_PREFERRED_NID()) {
                                    isPreferredTerm = true;
                                }
                            } else {
                                System.out.println("Can't convert: RefexCnidVersionBI:  " + rv);
                            }
                        } else {
                            System.out.println("Can't convert: RefexVersionBI:  " + refex);
                        }
                    }
                }
            }
            return isPreferredTerm;
        } catch (TerminologyException ex) {
            return isPreferredTerm;
        } catch (IOException ex) {
            return isPreferredTerm;
        }
    }
}
