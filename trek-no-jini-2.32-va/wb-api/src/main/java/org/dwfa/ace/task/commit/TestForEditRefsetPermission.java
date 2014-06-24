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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_ConfigAceDb;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/commit", type = BeanType.TASK_BEAN),
                   @Spec(directory = "plugins/precommit", type = BeanType.TASK_BEAN),
                   @Spec(directory = "plugins/commit", type = BeanType.TASK_BEAN) })
public class TestForEditRefsetPermission extends AbstractExtensionTest {

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
    public List<AlertToDataConstraintFailure> test(I_ExtendByRef extension, boolean forCommit)
            throws TaskFailedException {
        try {
            ArrayList<AlertToDataConstraintFailure> alertList = new ArrayList<AlertToDataConstraintFailure>();

            I_TermFactory termFactory = Terms.get();
            I_ConfigAceFrame configFrame = termFactory.getActiveAceFrameConfig();
            I_ConfigAceDb configDb = configFrame.getDbConfig();
            I_GetConceptData userTopHierarchy = termFactory.getConcept(ArchitectonicAuxiliary.Concept.USER.getUids());

            I_GetConceptData activeUser = configDb.getUserConcept();
            if (activeUser == null || activeUser.equals(userTopHierarchy)) {
                // activeUser = userTopHierarchy;
                return alertList;
            }

            // two types of permissions to check - first are permissions
            // assigned under a user role, e.g. SC has owner permissions for
            // CAB.
            // second type are permissions granted to an individual. e.g. SC has
            // create refset permission for CAB.

            // add individual permissions
            Set<I_GetConceptData> permissibleRefsetParents = new HashSet<I_GetConceptData>();
            permissibleRefsetParents.addAll(getValidRefsetsFromIndividualUserPermissions(activeUser));

            // add user role permissions
            permissibleRefsetParents.addAll(getValidRefsetsFromRolePermissions(activeUser));

            AlertToDataConstraintFailure.ALERT_TYPE alertType;
            if (forCommit) {
                alertType = AlertToDataConstraintFailure.ALERT_TYPE.ERROR;
            } else {
                alertType = AlertToDataConstraintFailure.ALERT_TYPE.WARNING;
            }

            boolean foundMatch = false;

            I_GetConceptData refsetSpec = termFactory.getConcept(extension.getRefsetId());
            I_GetConceptData specifiesRefsetRel =
                    termFactory.getConcept(RefsetAuxiliary.Concept.SPECIFIES_REFSET.getUids());
            I_GetConceptData memberRefset = getLatestRelationshipTarget(refsetSpec, specifiesRefsetRel);
            if (memberRefset == null) { // not a refset spec being edited
                return alertList;
            }

            for (I_GetConceptData potentialParent : permissibleRefsetParents) {
                if (potentialParent.isParentOfOrEqualTo(memberRefset)) {
                    foundMatch = true;
                }
            }

            if (!foundMatch) {
                alertList.add(new AlertToDataConstraintFailure(alertType,
                    "<html>User does not have permission to edit<br>this refset.", memberRefset));
            }

            return alertList;

        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    public Set<I_GetConceptData> getValidRefsetsFromRolePermissions(I_GetConceptData concept) throws Exception {
        I_TermFactory termFactory = Terms.get();
        Set<I_GetConceptData> refsets = new HashSet<I_GetConceptData>();

        PositionSetReadOnly allPositions = getPositions(termFactory);
        I_IntSet activeStatuses = getActiveStatus(termFactory);

        I_GetConceptData editRefsetPermission =
                termFactory.getConcept(ArchitectonicAuxiliary.Concept.EDIT_REFSET.getUids());
        I_GetConceptData ownerRole = termFactory.getConcept(ArchitectonicAuxiliary.Concept.OWNER_ROLE.getUids());
        I_GetConceptData adminRole = termFactory.getConcept(ArchitectonicAuxiliary.Concept.ADMIN_ROLE.getUids());
        I_GetConceptData authorRole = termFactory.getConcept(ArchitectonicAuxiliary.Concept.AUTHOR_ROLE.getUids());
        I_GetConceptData smeRole = termFactory.getConcept(ArchitectonicAuxiliary.Concept.SME_ROLE.getUids());
        I_GetConceptData reviewerRole = termFactory.getConcept(ArchitectonicAuxiliary.Concept.REVIEWER_ROLE.getUids());
        I_IntSet roleAllowedTypes = termFactory.newIntSet();
        roleAllowedTypes.add(ownerRole.getConceptNid());
        roleAllowedTypes.add(adminRole.getConceptNid());
        roleAllowedTypes.add(authorRole.getConceptNid());
        roleAllowedTypes.add(smeRole.getConceptNid());
        roleAllowedTypes.add(reviewerRole.getConceptNid());

        I_IntSet isAAllowedTypes = termFactory.getActiveAceFrameConfig().getDestRelTypes();

        List<? extends I_RelTuple> roleRels =
                concept.getSourceRelTuples(activeStatuses, roleAllowedTypes, allPositions,
                    getFrameConfig().getPrecedence(), getFrameConfig().getConflictResolutionStrategy());

        for (I_RelTuple roleRel : roleRels) {

            I_GetConceptData roleType = termFactory.getConcept(roleRel.getTypeId());
            I_GetConceptData hierarchyPermission = termFactory.getConcept(roleRel.getC2Id());

            List<? extends I_RelTuple> permissionRels =
                    roleType.getDestRelTuples(activeStatuses, isAAllowedTypes, allPositions,
                        getFrameConfig().getPrecedence(), getFrameConfig().getConflictResolutionStrategy());

            for (I_RelTuple permissionRel : permissionRels) {
                I_GetConceptData permission = termFactory.getConcept(permissionRel.getC1Id());
                if (permission.equals(editRefsetPermission)) {
                    refsets.add(hierarchyPermission);
                }
            }
        }

        return refsets;
    }

    public Set<? extends I_GetConceptData> getValidRefsetsFromIndividualUserPermissions(I_GetConceptData concept)
            throws Exception {
        I_TermFactory termFactory = Terms.get();
        PositionSetReadOnly allPositions = getPositions(termFactory);
        I_IntSet activeStatuses = getActiveStatus(termFactory);
        I_GetConceptData createNewRefsetPermissionRel =
                termFactory.getConcept(ArchitectonicAuxiliary.Concept.EDIT_REFSET.getUids());
        I_IntSet allowedTypes = termFactory.newIntSet();
        allowedTypes.add(createNewRefsetPermissionRel.getConceptNid());

        Set<? extends I_GetConceptData> refsets =
                concept.getSourceRelTargets(activeStatuses, allowedTypes, allPositions,
                    getFrameConfig().getPrecedence(), getFrameConfig().getConflictResolutionStrategy());

        return refsets;
    }
}
