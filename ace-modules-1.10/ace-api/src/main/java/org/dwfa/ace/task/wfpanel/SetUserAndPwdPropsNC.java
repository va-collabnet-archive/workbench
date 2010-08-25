package org.dwfa.ace.task.wfpanel;

import java.io.IOException;
import java.io.ObjectOutputStream;

import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/wfpanel", type = BeanType.TASK_BEAN) })
public class SetUserAndPwdPropsNC extends SetUserAndPwdPropsPNC {
    private static final long serialVersionUID = 1;
    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            // nothing to read...
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    @Override
    protected boolean showPrevious() {
        return false;
    }
}
