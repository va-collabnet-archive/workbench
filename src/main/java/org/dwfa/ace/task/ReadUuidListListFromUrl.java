package org.dwfa.ace.task;

import java.beans.IntrospectionException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Reads list of UUID's from URL/File
 * @author Susan Castillo
 *
 */
@BeanList(specs = { @Spec(directory = "tasks/ace/dups", type = BeanType.TASK_BEAN) })

public class ReadUuidListListFromUrl extends AbstractTask {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 2;

    private String potDupUuidListPropName = ProcessAttachmentKeys.DUP_UUID_L2.getAttachmentKey();
    private String dupPotFileName = ProcessAttachmentKeys.POT_DUP_FILENAME.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(potDupUuidListPropName);
        out.writeObject(dupPotFileName);
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
        	potDupUuidListPropName = (String) in.readObject();
        	if (objDataVersion >= 2){
        		dupPotFileName = (String) in.readObject();
        	} else {
        		dupPotFileName = "luceneDups/dupPotMatchResults/dwfaDups.txt";
        	}
        } else {
            throw new IOException(
                    "Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker)
                         throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
                                throws TaskFailedException {
        try {
        	            
             List<List<UUID>> potDupUuidListOfLists = new ArrayList<List<UUID>>();
             
            String uuidLineStr;
 		
 //			worker.getLogger().info("file is: " + dupPotFileName); 			
            
             BufferedReader br = new BufferedReader(new FileReader(dupPotFileName));
             
             while ((uuidLineStr = br.readLine()) != null) { // while loop begins here
            	 List<UUID> uuidList = new ArrayList<UUID>();
            	 for (String uuidStr: uuidLineStr.split("\t")){
         			 worker.getLogger().info("uuidStrs: " + uuidStr); 	
            		 UUID uuid = UUID.fromString(uuidStr);
            		 uuidList.add(uuid);
            	 }
            	 potDupUuidListOfLists.add(uuidList);
             } // end while 
             

            process.setProperty(this.potDupUuidListPropName, potDupUuidListOfLists);

            return Condition.CONTINUE;
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
            throw new TaskFailedException(e);
        } catch (FileNotFoundException e) {
        	throw new TaskFailedException(e);
		} catch (IOException e) {
			throw new TaskFailedException(e);
		} 
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

	public String getPotDupUuidListPropName() {
		return potDupUuidListPropName;
	}

	public void setPotDupUuidListPropName(String potDupUuidList) {
		this.potDupUuidListPropName = potDupUuidList;
	}

	public String getDupPotFileName() {
		return dupPotFileName;
	}

	public void setDupPotFileName(String dupPotFileName) {
		this.dupPotFileName = dupPotFileName;
	}

 
}
