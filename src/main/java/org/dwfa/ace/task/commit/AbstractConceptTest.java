package org.dwfa.ace.task.commit;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Transact;
import org.dwfa.bpa.process.TaskFailedException;

public abstract class AbstractConceptTest extends AbstractDataConstraintTest {
    
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
    public final List<AlertToDataConstraintFailure> test(I_Transact component, 
    		boolean forCommit) throws TaskFailedException {
        if (I_GetConceptData.class.isAssignableFrom(component.getClass())) {
            return test((I_GetConceptData) component, forCommit);
        }
        return new ArrayList<AlertToDataConstraintFailure>();
    }
    
    public abstract List<AlertToDataConstraintFailure> test(I_GetConceptData concept, 
    		boolean forCommit) throws TaskFailedException;
}
