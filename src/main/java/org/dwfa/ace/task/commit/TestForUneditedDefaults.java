package org.dwfa.ace.task.commit;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ace/commit", type = BeanType.TASK_BEAN),
        @Spec(directory = "plugins/precommit", type = BeanType.TASK_BEAN),
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
    public List<AlertToDataConstraintFailure> test(I_GetConceptData concept, 
    		boolean forCommit)
            throws TaskFailedException {
    	List<AlertToDataConstraintFailure> alerts = new ArrayList<AlertToDataConstraintFailure>();
        try {
            for (I_DescriptionVersioned desc: concept.getDescriptions()) {
            	alerts.addAll(testDescription(desc, forCommit));
            }
            for (I_DescriptionVersioned desc: concept.getUncommittedDescriptions()) {
            	alerts.addAll(testDescription(desc, forCommit));
            }
        } catch (IOException e) {
            throw new TaskFailedException(e);
        }
        return alerts;
    }

    private List<AlertToDataConstraintFailure> testDescription(I_DescriptionVersioned desc, boolean forCommit) {
        for (I_DescriptionPart part: desc.getVersions()) {
            if (part.getVersion() == Integer.MAX_VALUE) {
                if (part.getText().equalsIgnoreCase("New Fully Specified Description") || 
                        part.getText().equalsIgnoreCase("New Preferred Description")) {
                    String alertString = "<html>Unedited default description found: <font color='blue'>" + part.getText()
                                      + "</font><br>Please edit this value appropriately before commit...";
                    AlertToDataConstraintFailure.ALERT_TYPE alertType = AlertToDataConstraintFailure.ALERT_TYPE.WARNING;
                    if (forCommit) {
                         alertType = AlertToDataConstraintFailure.ALERT_TYPE.ERROR;
                    }
                    AlertToDataConstraintFailure alert = new AlertToDataConstraintFailure(alertType,
                    		alertString);
                    ArrayList<AlertToDataConstraintFailure> alertList = new ArrayList<AlertToDataConstraintFailure>();
                    alertList.add(alert);
                    return alertList;
                }
            }
        }
        return new ArrayList<AlertToDataConstraintFailure>();
    }

}
