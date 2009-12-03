package org.dwfa.ace.task.commit;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.task.classify.SnoTable;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = {
        @Spec(directory = "tasks/ide/commit", type = BeanType.TASK_BEAN),
        @Spec(directory = "plugins/precommit", type = BeanType.TASK_BEAN),
        @Spec(directory = "plugins/commit", type = BeanType.TASK_BEAN) })
public class TestForIsaCycle extends AbstractConceptTest {

    private static final long serialVersionUID = 1;
    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            //
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    @Override
    public List<AlertToDataConstraintFailure> test(I_GetConceptData concept,
            boolean forCommit) throws TaskFailedException {
        ArrayList<AlertToDataConstraintFailure> alertList = new ArrayList<AlertToDataConstraintFailure>();

        List<I_RelVersioned> usrl = concept.getUncommittedSourceRels();

        boolean foundCycle = false;
        for (I_RelVersioned rv : usrl) {
            List<I_RelTuple> rvtl = rv.getTuples();
            for (I_RelTuple rt : rvtl) {
                try {
                    
                    boolean test = SnoTable.findIsaCycle(rt.getC1Id(), rt
                            .getTypeId(), rt.getC2Id());
                    if (test)
                        foundCycle = true;
                } catch (TerminologyException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        if (foundCycle)
            alertList.add(new AlertToDataConstraintFailure(
                    AlertToDataConstraintFailure.ALERT_TYPE.WARNING,
                    "<html>Added IS_A relationship will create a cycle. ",
                    concept));

        return alertList;
    }

}
