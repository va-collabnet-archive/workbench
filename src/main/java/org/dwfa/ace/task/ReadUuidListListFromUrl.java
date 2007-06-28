package org.dwfa.ace.task;

import java.beans.IntrospectionException;
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
@BeanList(specs = { @Spec(directory = "tasks/ace", type = BeanType.TASK_BEAN) })

public class ReadUuidListListFromUrl extends AbstractTask {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String potDupUuidListPropName = ProcessAttachmentKeys.DUP_UUID_L2.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(potDupUuidListPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
        	potDupUuidListPropName = (String) in.readObject();
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
             
             UUID[] hardCodedUids = new UUID[] {
            		 UUID.fromString("4830c8a8-88b4-5d5b-b6e6-da78e2bb60bd"),
            		 UUID.fromString("d937dfd2-79bb-52de-b3fd-7f484cec44c4"),
            		 UUID.fromString("bcd682c4-8c31-543e-b23d-ab2f96667a96"),
            		 UUID.fromString("00eac716-5b5c-50c8-bad5-e3606df5ff49"),
            		 UUID.fromString("a2e18236-3fed-52f7-805f-11808840783a"),
            		 UUID.fromString("184508e9-a881-5d75-99b1-325f880c81d1"),
            		 UUID.fromString("9a7dec5f-38f6-5cf8-84bb-bdc977284429"),
             };
            
             for (UUID uuid: hardCodedUids) {
                 List<UUID> uuidList = new ArrayList<UUID>();
                 potDupUuidListOfLists.add(uuidList);
                 uuidList.add(uuid);
             }

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

 
}
