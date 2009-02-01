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

@BeanList(specs = { @Spec(directory = "tasks/ide/commit", type = BeanType.TASK_BEAN),
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
            	alerts.addAll(testDescription(concept, desc, forCommit));
            }
            for (I_DescriptionVersioned desc: concept.getUncommittedDescriptions()) {
            	alerts.addAll(testDescription(concept, desc, forCommit));
            }
        } catch (IOException e) {
            throw new TaskFailedException(e);
        }
        return alerts;
    }

    private List<AlertToDataConstraintFailure> testDescription(I_GetConceptData concept, I_DescriptionVersioned desc, boolean forCommit) {
        for (I_DescriptionPart part: desc.getVersions()) {
            if (part.getVersion() == Integer.MAX_VALUE) {
                if (part.getText().equalsIgnoreCase("New Fully Specified Description") || 
                        part.getText().equalsIgnoreCase("New Preferred Description") || 
                        part.getText().equalsIgnoreCase("New Description") ||
                        part.getText().toLowerCase().startsWith("clone of ")) {
                    String alertString = "<html>Unedited default found:<br> <font color='blue'>" + part.getText()
                                      + "</font><br>Please edit this value before commit...";
                    AlertToDataConstraintFailure.ALERT_TYPE alertType = AlertToDataConstraintFailure.ALERT_TYPE.WARNING;
                    if (forCommit) {
                         alertType = AlertToDataConstraintFailure.ALERT_TYPE.ERROR;
                    }
                    AlertToDataConstraintFailure alert = new AlertToDataConstraintFailure(alertType,
                    		alertString, concept);
                    
                    if (part.getText().equalsIgnoreCase("New Description")) {
                    	AbortDescriptionPart fixup = new AbortDescriptionPart(concept,
                    			desc, part);
                    	alert.getFixOptions().add(fixup);
                    }
                    
                    ArrayList<AlertToDataConstraintFailure> alertList = new ArrayList<AlertToDataConstraintFailure>();
                    alertList.add(alert);
                    return alertList;
                }
            }
        }
        return new ArrayList<AlertToDataConstraintFailure>();
    }

}
