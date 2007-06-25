package org.dwfa.ace.task.path;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ace/path", type = BeanType.TASK_BEAN) })
public class NewEditPathForUser extends AbstractTask {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;
    
     private String editPathConceptPropName = ProcessAttachmentKeys.EDIT_PATH_CONCEPT.getAttachmentKey();

     private String userPropName = ProcessAttachmentKeys.USERNAME.getAttachmentKey();

     private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(editPathConceptPropName);
        out.writeObject(userPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            editPathConceptPropName = (String) in.readObject();
            userPropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        // Nothing to do...

    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        try {
            String username = (String) process.readProperty(userPropName);
            
            //I_GetConceptData newConcept = LocalVersionedTerminology.get().newConcept(UUID.randomUUID(), false, config);

            
            I_GetConceptData editPathConcept = (I_GetConceptData) process.readProperty(editPathConceptPropName);

            
            I_Path editPath = LocalVersionedTerminology.get().getPath(editPathConcept.getUids());

            process.setProperty(editPathConceptPropName, editPathConcept);
            //profile.addEditingPath(editPath);

            return Condition.CONTINUE;
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        } catch (IOException e) {
            throw new TaskFailedException(e);
        } catch (TerminologyException e) {
            throw new TaskFailedException(e);
        }
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }


    public String getUserPropName() {
        return userPropName;
    }

    public void setUserPropName(String profilePropName) {
        this.userPropName = profilePropName;
    }

    public String getEditPathConceptPropName() {
        return editPathConceptPropName;
    }

    public void setEditPathConceptPropName(String editPathEntry) {
        this.editPathConceptPropName = editPathEntry;
    }

}
