package org.dwfa.ace.task;

import java.beans.IntrospectionException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
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

@BeanList(specs = { @Spec(directory = "tasks/ace/assignments", type = BeanType.TASK_BEAN) })
public class WriteUuidListListToDisk extends AbstractTask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int dataVersion = 1;
	
    private String uuidListListPropName = ProcessAttachmentKeys.UUID_LIST_LIST.getAttachmentKey();
    
    private String fileName = "uuidListList.txt";


	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(uuidListListPropName);
		out.writeObject(fileName);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == dataVersion) {
			uuidListListPropName = (String) in.readObject();
			fileName = (String) in.readObject();
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
			List<List<UUID>> idListList = (List<List<UUID>>) process.readProperty(uuidListListPropName);
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
			for (List<UUID> idList: idListList) {
				for (int i = 0; i < idList.size(); i++) {
					UUID id = idList.get(i);
					writer.append(id.toString());
					if (i < idList.size() - 1) {
						writer.append("\t");
					}
				}
				writer.append("\n");
			}
			writer.close();
			return Condition.CONTINUE;
		} catch (IOException e) {
			throw new TaskFailedException(e);
		} catch (IntrospectionException e) {
			throw new TaskFailedException(e);
		} catch (IllegalAccessException e) {
			throw new TaskFailedException(e);
		} catch (InvocationTargetException e) {
			throw new TaskFailedException(e);
		}
	}

	public Collection<Condition> getConditions() {
		return CONTINUE_CONDITION;
	}

	public int[] getDataContainerIds() {
		return new int[] {};
	}

	public String getUuidListListPropName() {
		return uuidListListPropName;
	}

	public void setUuidListListPropName(String uuidListListPropName) {
		this.uuidListListPropName = uuidListListPropName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
