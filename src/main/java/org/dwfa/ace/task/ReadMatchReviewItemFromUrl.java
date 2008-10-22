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
 * Reads match review item from URL/File
 * 
 * @author Eric Mays (EKM)
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/ace/assignments", type = BeanType.TASK_BEAN) })
public class ReadMatchReviewItemFromUrl extends AbstractTask {

	/**
     *
     */
	private static final long serialVersionUID = 1L;

	private static final int dataVersion = 1;

	private String uuidListListPropName = ProcessAttachmentKeys.UUID_LIST_LIST
			.getAttachmentKey();
	private String inputFileNamePropName = ProcessAttachmentKeys.DEFAULT_FILE
			.getAttachmentKey();
	private String htmlPropName = ProcessAttachmentKeys.HTML_STR
			.getAttachmentKey();

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(uuidListListPropName);
		out.writeObject(inputFileNamePropName);
		out.writeObject(htmlPropName);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion <= dataVersion) {
			uuidListListPropName = (String) in.readObject();
			inputFileNamePropName = (String) in.readObject();
			htmlPropName = (String) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
	}

	public void complete(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		// Nothing to do
	}

	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		try {

			List<List<UUID>> uuidListOfLists = new ArrayList<List<UUID>>();
			String term = "";
			String html = "";

			// worker.getLogger().info("file is: " + uuidFileName);
			String inputFileName = (String) process
					.readProperty(inputFileNamePropName);
			BufferedReader br = new BufferedReader(
					new FileReader(inputFileName));
			String line;
			int i = 0;
			while ((line = br.readLine()) != null) {
				i++;
				if (i == 1) {
					term = line;
					html += "<h2>" + term + "</h2>";
					continue;
				}
				List<UUID> uuidList = new ArrayList<UUID>();
				String[] fields = line.split("\t");
				html += "<br>" + fields[0] + "<br>" + fields[1];
				String uuidStr = fields[3];
				worker.getLogger().info("uuidStrs: " + uuidStr);
				UUID uuid = UUID.fromString(uuidStr);
				uuidList.add(uuid);
				uuidListOfLists.add(uuidList);
			}

			process.setProperty(this.uuidListListPropName, uuidListOfLists);
			process.setProperty(this.htmlPropName, html);

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

	public String getUuidListListPropName() {
		return uuidListListPropName;
	}

	public void setUuidListListPropName(String potDupUuidList) {
		this.uuidListListPropName = potDupUuidList;
	}

	public String getInputFileNamePropName() {
		return inputFileNamePropName;
	}

	public void setInputFileNamePropName(String fileName) {
		this.inputFileNamePropName = fileName;
	}

	public String getHtmlPropName() {
		return htmlPropName;
	}

	public void setHtmlPropName(String htmlPropName) {
		this.htmlPropName = htmlPropName;
	}

}
