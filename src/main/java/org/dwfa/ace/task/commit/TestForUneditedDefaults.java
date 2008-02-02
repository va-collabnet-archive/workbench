package org.dwfa.ace.task.commit;

import java.io.IOException;
import java.io.ObjectOutputStream;

import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ace/commit", type = BeanType.TASK_BEAN),
        @Spec(directory = "plugins/commit", type = BeanType.TASK_BEAN)})
public class TestForUneditedDefaults extends AbstractConceptTest {

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
    public boolean test(I_GetConceptData concept, I_AlertToDataConstraintFailure alertObject, boolean forCommit)
            throws TaskFailedException {
        boolean success = true;
        try {
            for (I_DescriptionVersioned desc: concept.getDescriptions()) {
                if (testDescription(alertObject, desc, forCommit) == false) {
                    success = false;
                }
            }
            for (I_DescriptionVersioned desc: concept.getUncommittedDescriptions()) {
                if (testDescription(alertObject, desc, forCommit) == false) {
                    success = false;
                }
            }
        } catch (IOException e) {
            throw new TaskFailedException(e);
        }
        return success;
    }

    private boolean testDescription(I_AlertToDataConstraintFailure alertObject, I_DescriptionVersioned desc, boolean forCommit) {
        for (I_DescriptionPart part: desc.getVersions()) {
            if (part.getVersion() == Integer.MAX_VALUE) {
                if (part.getText().equalsIgnoreCase("New Fully Specified Description") || 
                        part.getText().equalsIgnoreCase("New Preferred Description")) {
                    alertObject.alert("Unedited default description found: " + part.getText()
                                      + "\nPlease edit this value appropriately before commit...");
                    return false;
                }
            }
        }
        return true;
    }

}
