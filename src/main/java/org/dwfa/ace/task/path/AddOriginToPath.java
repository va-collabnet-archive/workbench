package org.dwfa.ace.task.path;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.util.Collection;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.tapi.AllowDataCheckSuppression;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * @author Ming Zhang
 * 
 * @created 15/01/2008
 */
@AllowDataCheckSuppression
@BeanList(specs = { @Spec(directory = "tasks/ide/path", type = BeanType.TASK_BEAN) })
public class AddOriginToPath extends AbstractTask {

    private static final long serialVersionUID = 1L;

    private static final int DATA_VERSION = 1;

    private static final String LATEST_VERSION_STR = "latest";
    
    private static final String PRESENT_TIME_STR = "now";
    
    private String pathConceptPropName = ProcessAttachmentKeys.TO_PATH_CONCEPT.getAttachmentKey();

    private String originPathConceptPropName = ProcessAttachmentKeys.FROM_PATH_CONCEPT.getAttachmentKey();

    private String originPositionStr = LATEST_VERSION_STR;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(DATA_VERSION);
        out.writeObject(pathConceptPropName);
        out.writeObject(originPathConceptPropName);
        out.writeObject(originPositionStr);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= DATA_VERSION) {
            pathConceptPropName = (String) in.readObject();
            originPathConceptPropName = (String) in.readObject();
            originPositionStr = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do...

    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {

            I_GetConceptData subjectPathConcept = (I_GetConceptData) process.readProperty(pathConceptPropName);
            I_GetConceptData originPathConcept = (I_GetConceptData) process.readProperty(originPathConceptPropName);
            
            if (subjectPathConcept == null) {
                throw new TaskFailedException("A path concept has not be specified.");
            }
            
            if (originPathConcept == null) {
                throw new TaskFailedException("An origin path concept has not be specified.");
            }            
            
            if (originPositionStr == null || originPositionStr.isEmpty()) {
                throw new TaskFailedException("A origin position (time) has not be specified.");
            }
            
            I_TermFactory termFactory = LocalVersionedTerminology.get();
            int version;

            try {
                if (LATEST_VERSION_STR.equalsIgnoreCase(originPositionStr)) {
                    version = Integer.MAX_VALUE;
                } else if (PRESENT_TIME_STR.equalsIgnoreCase(originPositionStr)) {
                    version = termFactory.convertToThinVersion(System.currentTimeMillis());
                } else {
                    version = termFactory.convertToThinVersion(originPositionStr);
                }            
            } catch (ParseException e) {
                throw new TaskFailedException("Invalid position (time): '" + originPositionStr + "'.", e);
            }

            I_Path subjectPath = termFactory.getPath(subjectPathConcept.getUids());
            I_Path originPath = termFactory.getPath(originPathConcept.getUids());
            
            subjectPath.addOrigin(termFactory.newPosition(originPath, version));
            
            return Condition.CONTINUE;

        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }
    
    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public String getPathConceptPropName() {
        return pathConceptPropName;
    }

    public void setPathConceptPropName(String pathConceptPropName) {
        this.pathConceptPropName = pathConceptPropName;
    }

    public String getOriginPathConceptPropName() {
        return originPathConceptPropName;
    }

    public void setOriginPathConceptPropName(String originPathConceptPropName) {
        this.originPathConceptPropName = originPathConceptPropName;
    }

    public String getOriginPositionStr() {
        return originPositionStr;
    }

    public void setOriginPositionStr(String originPositionStr) {
        this.originPositionStr = originPositionStr;
    }



}
