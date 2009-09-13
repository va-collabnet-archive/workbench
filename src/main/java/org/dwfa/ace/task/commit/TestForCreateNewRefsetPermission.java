package org.dwfa.ace.task.commit;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
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
            I_TermFactory termFactory = LocalVersionedTerminology.get();
            I_ConfigAceFrame activeProfile = termFactory.getActiveAceFrameConfig();

            // TODO how to get active user
            I_GetConceptData user = termFactory.getConcept(ArchitectonicAuxiliary.Concept.KEITH_CAMPBELL.getUids());
            // user = (I_GetConceptData)
            // activeProfile.getProperty(activeProfile.getUsername());
            // System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + user +
            // "<<<<<<<<<<<<<<<<<<<<<<<<<<");

            // check if this concept is a child of the refset identity concept -
            // if it isn't, then there is no alert
            I_GetConceptData refsetIdentity = termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_IDENTITY.getUids());
            if (!refsetIdentity.isParentOf(concept, true)) {
                return alertList;
            }

            // find the refset (parents) that this user can create content under
            Set<I_Position> allPositions = getPositions(termFactory);
            I_IntSet activeStatuses = getActiveStatus(termFactory);
            I_GetConceptData createNewRefsetPermissionRel =
                    termFactory.getConcept(ArchitectonicAuxiliary.Concept.CREATE_NEW_REFSET_PERMISSION.getUids());
            I_IntSet allowedTypes = termFactory.newIntSet();
            allowedTypes.add(createNewRefsetPermissionRel.getConceptId());
            Set<I_GetConceptData> permissibleRefsetParents =
                    user.getSourceRelTargets(activeStatuses, allowedTypes, allPositions, true, true);

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
            }

            return alertList;

        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }
}