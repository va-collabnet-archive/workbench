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
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/commit", type = BeanType.TASK_BEAN),
                   @Spec(directory = "plugins/precommit", type = BeanType.TASK_BEAN),
                   @Spec(directory = "plugins/commit", type = BeanType.TASK_BEAN) })
public class TestForCreateNewRefsetPermission extends AbstractConceptTest {

    private static final long serialVersionUID = 1;
    private static final int dataVersion = 1;
    private I_GetConceptData activeUser;
    private I_TermFactory termFactory;
    private I_ConfigAceDb configDb;
    private I_ConfigAceFrame configFrame;

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
        try {
            ArrayList<AlertToDataConstraintFailure> alertList = new ArrayList<AlertToDataConstraintFailure>();
            if (true) {
                return alertList;
            }

            termFactory = LocalVersionedTerminology.get();
            configFrame = termFactory.getActiveAceFrameConfig();
            configDb = configFrame.getDbConfig();
            I_GetConceptData userTopHierarchy = termFactory.getConcept(ArchitectonicAuxiliary.Concept.USER.getUids());

            activeUser = configDb.getUserConcept();
            if (activeUser == null || activeUser.equals(userTopHierarchy)) {
                System.out.println("************ : " + activeUser);
                return alertList;
                // activeUser = userTopHierarchy;
            }
            System.out.println("************************: " + activeUser);

            // check if this concept is a child of the refset identity concept -
            // if it isn't, then there is no alert
            I_GetConceptData refsetIdentity = termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_IDENTITY.getUids());
            if (!refsetIdentity.isParentOf(concept, true)) {

                System.out.println(concept + " IS NOT A CHILD OF " + refsetIdentity);
                return alertList;
            } else {
                System.out.println(concept + " IS A CHILD OF " + refsetIdentity);
            }

            // two types of permissions to check - first are permissions
            // assigned under a user role, e.g. SC has owner permissions for
            // CAB.
            // second type are permissions granted to an individual. e.g. SC has
            // create refset permission for CAB.

            // add individual permissions
            Set<I_GetConceptData> permissibleRefsetParents = new HashSet<I_GetConceptData>();
            permissibleRefsetParents.addAll(getValidRefsetsFromIndividualUserPermissions());

            // add user role permissions
            permissibleRefsetParents.addAll(getValidRefsetsFromRolePermissions());

            AlertToDataConstraintFailure.ALERT_TYPE alertType;
            if (forCommit) {
                alertType = AlertToDataConstraintFailure.ALERT_TYPE.ERROR;
            } else {
                alertType = AlertToDataConstraintFailure.ALERT_TYPE.WARNING;
            }

            boolean foundMatch = false;
            for (I_GetConceptData potentialParent : permissibleRefsetParents) {
                if (potentialParent.isParentOf(concept, true)) {
                    foundMatch = true;
                }
            }

            if (!foundMatch) {
                alertList.add(new AlertToDataConstraintFailure(alertType,
                    "<html>User does not have permission to create<br>a new refset in this hierarchy.", concept));
                System.out.println("User doesn't have permission ____________");
            } else {
                System.out.println("user has permission !!!!!!!!!!!!");
            }

            return alertList;

        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    private Set<I_GetConceptData> getValidRefsetsFromRolePermissions() throws Exception {
        Set<I_GetConceptData> refsets = new HashSet<I_GetConceptData>();

        Set<I_Position> allPositions = getPositions(termFactory);
        I_IntSet activeStatuses = getActiveStatus(termFactory);
        I_GetConceptData type = termFactory.getConcept(ArchitectonicAuxiliary.Concept.GRANTED_ROLE.getUids());
        I_IntSet allowedTypes = termFactory.newIntSet();
        allowedTypes.add(type.getConceptId());

        List<I_RelTuple> roles = activeUser.getSourceRelTuples(activeStatuses, allowedTypes, allPositions, true, true);

        I_GetConceptData createNewRefsetPermission =
                termFactory.getConcept(ArchitectonicAuxiliary.Concept.CREATE_NEW_REFSET_PERMISSION.getUids());

        for (I_RelTuple roleRel : roles) {
            int relationshipId = roleRel.getRelId();
            List<I_ThinExtByRefVersioned> extensions = termFactory.getAllExtensionsForComponent(relationshipId, true);
            // TODO filter on refset type
            I_GetConceptData extConcept = null;

            for (I_ThinExtByRefVersioned currExt : extensions) {

                List<I_ThinExtByRefTuple> tuples = currExt.getTuples(activeStatuses, allPositions, true, true);

                if (tuples.size() > 0) {
                    I_ThinExtByRefPart thinPart = tuples.get(0).getPart();

                    if (thinPart instanceof I_ThinExtByRefPartConcept) {
                        I_ThinExtByRefPartConcept part = (I_ThinExtByRefPartConcept) thinPart;
                        extConcept = termFactory.getConcept(part.getC1id());
                    }
                }
            }
            if (extConcept != null) {

                I_GetConceptData c2 = termFactory.getConcept(roleRel.getC2Id());
                I_GetConceptData isAType = termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids());
                I_IntSet isAAllowedTypes = termFactory.newIntSet();
                isAAllowedTypes.add(isAType.getConceptId());

                List<I_RelTuple> permissions =
                        c2.getDestRelTuples(activeStatuses, isAAllowedTypes, allPositions, true, true);

                for (I_RelTuple permissionRel : permissions) {
                    if (permissionRel.getC1Id() == createNewRefsetPermission.getConceptId()) {
                        refsets.add(extConcept);
                    }
                }
            }

        }

        return refsets;
    }

    private Set<I_GetConceptData> getValidRefsetsFromIndividualUserPermissions() throws Exception {
        Set<I_Position> allPositions = getPositions(termFactory);
        I_IntSet activeStatuses = getActiveStatus(termFactory);
        I_GetConceptData createNewRefsetPermissionRel =
                termFactory.getConcept(ArchitectonicAuxiliary.Concept.CREATE_NEW_REFSET_PERMISSION.getUids());
        I_IntSet allowedTypes = termFactory.newIntSet();
        allowedTypes.add(createNewRefsetPermissionRel.getConceptId());

        Set<I_GetConceptData> refsets =
                activeUser.getSourceRelTargets(activeStatuses, allowedTypes, allPositions, true, true);

        return refsets;
    }
}