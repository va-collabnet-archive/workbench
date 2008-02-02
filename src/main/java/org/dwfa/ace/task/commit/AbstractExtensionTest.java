package org.dwfa.ace.task.commit;

import java.io.IOException;
import java.io.ObjectOutputStream;

import org.dwfa.ace.api.I_Transact;
import org.dwfa.ace.api.ebr.I_GetExtensionData;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.bpa.process.TaskFailedException;

public abstract class AbstractExtensionTest extends AbstractDataConstraintTest {

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
    public final boolean test(I_Transact component, I_AlertToDataConstraintFailure alertObject, boolean forCommit) throws TaskFailedException {
        if (I_GetExtensionData.class.isAssignableFrom(component.getClass())) {
            I_GetExtensionData extension = (I_GetExtensionData) component;
            try {
                return test(extension.getExtension(), alertObject, forCommit);
            } catch (IOException e) {
                throw new TaskFailedException(e);
            }
        } else if (I_ThinExtByRefVersioned.class.isAssignableFrom(component.getClass())) {
            return test((I_ThinExtByRefVersioned) component, alertObject, forCommit);
        } 
        return true;
    }
    
    public abstract boolean test(I_ThinExtByRefVersioned extension, I_AlertToDataConstraintFailure alertObject, boolean forCommit) throws TaskFailedException;
}
