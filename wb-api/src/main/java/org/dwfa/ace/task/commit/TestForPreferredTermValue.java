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
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.commit.failureconstraintfactory.AlertToDataConstraintFailureFactory;
import org.dwfa.ace.task.commit.failureconstraintfactory.SimpleConstraintFailureChooser;
import org.dwfa.ace.task.commit.validator.GetConceptDataValidationStrategy;
import org.dwfa.ace.task.commit.validator.ValidationException;
import org.dwfa.ace.task.commit.validator.impl.NotEmptyConceptDataValidator;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.spec.ConceptSpec;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_nid.RefexNidVersionBI;
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
            List<AlertToDataConstraintFailure> alerts = new ArrayList<AlertToDataConstraintFailure>();
            /*
             * Use I_TermFactory to get the view coordiate in the active frame.
             * There is not currently a way to do this using TerminologySnapshotDI.
             * If there is another way to get the view coordinate, which does not depend
             * on the active frame, that is preferable
             */
            ViewCoordinate vc = Terms.get().getActiveAceFrameConfig().getViewCoordinate();
            /*
             * Use SnomedMetadatRfx to get the rf1 or rf2 version of the concept
             * depending on what the database is using. After getting the concept spec use
             * getStrict to get either the ConceptChronicle, any ids, or any versioned components.
             * This will ensure that the the values returned refer to an active concept.
             */
            ConceptChronicleBI requiredConcept = SnomedMetadataRfx.getDESC_PREFERRED().getStrict(vc).getChronicle();
            /*
             * Getting the ConceptVersion of the concept which was passed in.
             * Got the version instead of the chronicle because the active
             * descriptions are needed below. The version already contains the 
             * information about which components are active.
             */
            ConceptVersionBI cv = Ts.get().getConceptVersion(vc, concept.getNid());
            /*
             * Now getting the active descriptions is easy. Only the active
             * descriptions are important to test for duplicates, etc.
             */
            Collection<? extends DescriptionVersionBI> descsActive = cv.getDescriptionsActive();

            GetConceptDataValidationStrategy validator = new NotEmptyConceptDataValidator(requiredConcept,
                    descsActive, concept);
            try {
                validator.validate();
            } catch (ValidationException e) {
                getLogger().info(e.getMessage());
                alerts.add(getAlertFactory(forCommit).createAlertToDataConstraintFailure(
                        String.format(ALERT_MESSAGE, requiredConcept.toString()), concept));
            }
            int gbPrefCount = 0;
            int usPrefCount = 0;
            /*
             * Use concept specs to reference.
             */
            ConceptSpec EN_GB_LANG =
                    new ConceptSpec("GB English Dialect Subset",
                    UUID.fromString("a0982f18-ec51-56d2-a8b1-6ff8964813dd"));
            ConceptSpec EN_US_LANG =
                    new ConceptSpec("US English Dialect Subset",
                    UUID.fromString("29bf812c-7a77-595d-8b12-ea37c473a5e6"));
            
            for (DescriptionVersionBI desc : descsActive) {
                /*
                 * Getting the active status nid and the description type nid
                 * from the Rfx method. The type nid determins if the description 
                 * is a FSN or synonym.
                 */
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

    private boolean isPreferredTerm(DescriptionVersionBI desc, ConceptSpec evalRefset) {
        boolean isPreferredTerm = false;
        try {
            Collection<? extends RefexChronicleBI> refexes =
                    desc.getRefexesActive(Terms.get().getActiveAceFrameConfig().getViewCoordinate());
            /*
             * Can use getNidForUuids to convert betwee nid and uuid.
             */
            int evalRefsetNid = Ts.get().getNidForUuids(evalRefset.getUuids());

            if (refexes != null) {
                for (RefexChronicleBI refex : refexes) {
                    if (refex.getRefexNid() == evalRefsetNid) {
                        if (RefexVersionBI.class.isAssignableFrom(refex.getClass())) {
                            RefexVersionBI<?> rv = (RefexVersionBI<?>) refex;
                            if (RefexNidVersionBI.class.isAssignableFrom(rv.getClass())) {
                                /*
                                 * Synonyms are stored in the dialect refexes with either a value
                                 * of preferred, indicating a preferred term, or acceptable.
                                 * The type of refex in this case is concept refsex (RefexNidVersionBI), 
                                 * and the value of preferred or acceptable is stored in cnid1.
                                 * See org.ihtsdo.tk.api.refex.type_*
                                 */
                                int cnid = ((RefexNidVersionBI) rv).getNid1();
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
