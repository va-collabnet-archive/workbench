package org.dwfa.ace.task.data.checks;

import java.beans.IntrospectionException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.refset.RefSetConflictValidator;
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




//TODO: write java docs.
/**
 * <h1>VerifyNoRefSetConflicts</h1>
 * <br>
 * <p>The <code>VerifyNoRefSetConflicts</code> class checks for conflicts between concepts in 
 *    the hierarchy.</P>
 * 
 * <br>
 * <br>
 * @see <code>org.dwfa.bpa.tasks.AbstractTask</code>
 * @author PeterVawser
 *
 */
@BeanList(specs = { @Spec(directory = "tasks/ace/data checks", type = BeanType.TASK_BEAN) })
public class VerifyNoRefSetConflicts extends AbstractTask {
	/*
	 * Priavte instance variables
	 */
	private static final long serialVersionUID = 1;
	private static final int dataVersion = 2;
	
	private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }// End method writeObject

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
        	//Do nothing...
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }// End method readObject
    
    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
    	try{    	
    		
    		RefSetConflictValidator validator = new RefSetConflictValidator();
    		validator.validate();
    		
    		if(validator.hasConflicts()){  			   			
    			return Condition.FALSE;
    		}
    		   		    		    		
    	}
    	catch(IOException e){throw new TaskFailedException(e);}
    	catch(TerminologyException e){throw new TaskFailedException(e);}
    	 
    	return Condition.TRUE;
    }//End method evaluate
    
    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do

    }//End method complete

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return AbstractTask.CONDITIONAL_TEST_CONDITIONS;
    }//End method getConditions

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
     */
    public int[] getDataContainerIds() {
        return new int[] {};
    }//End getDataContainerIds	
		
}//End class VerifyNoRefSetConflicts