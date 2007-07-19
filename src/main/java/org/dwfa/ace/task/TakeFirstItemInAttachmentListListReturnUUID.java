package org.dwfa.ace.task;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
* Takes/removes the first item (UUID) in a specified attachment list of lists and returns a UUID.
* @author Susan Castillo
*
*/
@BeanList(specs = { @Spec(directory = "tasks/ace/assignments", type = BeanType.TASK_BEAN) })

public class TakeFirstItemInAttachmentListListReturnUUID extends AbstractTask {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String listListNamePropName = ProcessAttachmentKeys.UUID_LIST_LIST.getAttachmentKey();

    private String uuidListPropName = ProcessAttachmentKeys.UUID_LIST.getAttachmentKey();


    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(listListNamePropName);
        out.writeObject(uuidListPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
        	listListNamePropName = (String) in.readObject();
            uuidListPropName = (String) in.readObject();
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
            ArrayList<Collection<UUID>> temporaryList =
                (ArrayList<Collection<UUID>>) process.readProperty(listListNamePropName);

            if (worker.getLogger().isLoggable(Level.FINE)) {
                worker.getLogger().fine(("Removing first item in attachment list."));
            }

            Collection<UUID> uuid = temporaryList.remove(0);
			worker.getLogger().info("uuid: " + uuid);

            process.setProperty(this.uuidListPropName, uuid);
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

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

	public String getUuidListPropName() {
		return uuidListPropName;
	}

	public void setUuidListPropName(String uuidListPropName) {
		this.uuidListPropName = uuidListPropName;
	}

	public String getListListNamePropName() {
		return listListNamePropName;
	}

	public void setListListNamePropName(String listNamePropName) {
		this.listListNamePropName = listNamePropName;
	}

}

