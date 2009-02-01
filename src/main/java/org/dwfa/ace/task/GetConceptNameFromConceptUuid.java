package org.dwfa.ace.task;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.logging.Level;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
* Input Uuid and return  concept name.
* @author Susan Castillo
*
*/

@BeanList(specs = { @Spec(directory = "tasks/ide/assignments", type = BeanType.TASK_BEAN) })
public class GetConceptNameFromConceptUuid extends AbstractTask {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;
    
    private String uuidPropName = ProcessAttachmentKeys.ACTIVE_CONCEPT_UUID.getAttachmentKey();
    
    private String conceptPropName = ProcessAttachmentKeys.ACTIVE_CONCEPT.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(uuidPropName);
        out.writeObject(conceptPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            uuidPropName = (String) in.readObject();
            conceptPropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        // Nothing to do...

    }

    @SuppressWarnings("unchecked")
	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        try {
            
        	I_GetConceptData concept = AceTaskUtil.getConceptFromProperty(process, uuidPropName);
            
            if (worker.getLogger().isLoggable(Level.FINE)) {
                worker.getLogger().fine(("Removing first item in attachment list."));
            }
        
          //  worker.getLogger().info("uuidListPropName: " + uuidListPropName);
          // worker.getLogger().info("temporaryListUuid: " + temporaryListUuid);
                        

			//worker.getLogger().info("concept" + concept);

            process.setProperty(this.conceptPropName, concept);
            
            return Condition.CONTINUE;
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
            throw new TaskFailedException(e);
        } catch (TerminologyException e) {
        	throw new TaskFailedException(e);
		} catch (IOException e) {
			throw new TaskFailedException(e);
		}
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

	public String getConceptPropName() {
		return conceptPropName;
	}

	public void setConceptPropName(String conceptPropName) {
		this.conceptPropName = conceptPropName;
	}

	public String getUuidPropName() {
		return uuidPropName;
	}

	public void setUuidPropName(String uuidPropName) {
		this.uuidPropName = uuidPropName;
	}


}

