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
import java.util.List;

import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure.ALERT_TYPE;
import org.dwfa.ace.task.commit.failureconstraintfactory.AlertToDataConstraintFailureFactory;
import org.dwfa.ace.task.commit.failureconstraintfactory.SimpleConstraintFailureChooser;
import org.dwfa.ace.task.commit.validator.ConceptDescriptionFacade;
import org.dwfa.ace.task.commit.validator.GetConceptDataValidationStrategy;
import org.dwfa.ace.task.commit.validator.ValidationException;
import org.dwfa.ace.task.commit.validator.impl.NotEmptyConceptDataValidator;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeVersionBI;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

/**
 * The <code>TestForFsnValue</code> class represents a data constraint test that
 * is to be run on any <code>concept</code> to verify that it has a 'fully
 * specified name' that is not null and a value with a length
 * greater than <code>0</code>.
 * 
 * @author Matthew Edwards
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/commit", type = BeanType.TASK_BEAN)})
public class TestForSpecifcDate extends AbstractConceptTest {

    private static final long serialVersionUID = 1;
    private static final int DATA_VERSION = 1;

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
        	long timeToCheck = 1327996800000L;
            I_TermFactory termFactory = getTermFactory();
            List<AlertToDataConstraintFailure> alerts = new ArrayList<AlertToDataConstraintFailure>();
            boolean found = false;
            
            for (ConceptAttributeVersionBI attr : concept.getConceptAttributes().getVersions()) {
            	long time = attr.getTime();
            	if (time == timeToCheck) {
            		found = true;
            	}
            }
            
            for (DescriptionChronicleBI loopDesc : concept.getDescs()) {
            	for (DescriptionVersionBI dv : loopDesc.getVersions()) {
            		long time = dv.getTime();
            		if (time == timeToCheck) {
                		found = true;
                	}
            	}
            	for (RefexChronicleBI<?> annot : loopDesc.getAnnotations()) {
            		for (RefexVersionBI<?> av : annot.getVersions()) {
            			long time = av.getTime();
            			if (time == timeToCheck) {
                    		found = true;
                    	}
            		}
            	}
            }
            
            for (RelationshipChronicleBI loopRel : concept.getRelationshipsOutgoing()) {
            	for (RelationshipVersionBI rv : loopRel.getVersions()) {
            		long time = rv.getTime();
            		if (time == timeToCheck) {
                		found = true;
                	}
            	}
            }
            
            if (found) {
            	AlertToDataConstraintFailure alert = new AlertToDataConstraintFailure(ALERT_TYPE.WARNING, 
            			"Warning: Concept with Versions in last release", concept);
                alerts.add(alert);
            }

            return alerts;
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    private I_TermFactory getTermFactory() {
        return Terms.get();
    }
}
