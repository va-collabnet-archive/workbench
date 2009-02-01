package org.dwfa.ace.task.path;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Set;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.jini.TermEntry;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * @author Ming Zhang
 *
 * @created 18/01/2008
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/path", type = BeanType.TASK_BEAN) })
public class SetEditPathFromDescription extends AbstractTask {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;
    
     private String  PathDescription = "Use Attachement";
    

	private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(PathDescription);
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
        	PathDescription = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        // Nothing to do...

    }

    /* (non-Javadoc)
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        try {
        	String DescriptionForExistingPath = (String)process.readProperty(PathDescription);
            I_TermFactory tf = LocalVersionedTerminology.get();
            I_ConfigAceFrame frameConfig = tf.getActiveAceFrameConfig();
            for(I_Path path:tf.getPaths())
            {
            	worker.getLogger().info(Integer.toString(path.getConceptId()));
            	for (I_DescriptionVersioned description:tf.getConcept(path.getConceptId()).getDescriptions())
            	{
            		int id = ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid();
            		if(description.getLastTuple().getTypeId() == id 
            				&& description.getLastTuple().getText().equals(DescriptionForExistingPath))
            		{
            	            Set<I_Path> editSet = frameConfig.getEditingPathSet();
            	            editSet.clear();
            	            frameConfig.addEditingPath(path);
            	            return Condition.CONTINUE;
            		}
            	}
            }
            throw new TaskFailedException();
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } catch (IOException e) {
            throw new TaskFailedException(e);
        } catch (TerminologyException e) {
            throw new TaskFailedException(e);
        }
        catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public String getPathDescription() {
		return PathDescription;
	}

	public void setPathDescription(String pathDescription) {
		PathDescription = pathDescription;
	}

}
