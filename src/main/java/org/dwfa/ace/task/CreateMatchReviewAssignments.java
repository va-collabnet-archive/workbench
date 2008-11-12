package org.dwfa.ace.task;

import java.beans.IntrospectionException;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.dwfa.bpa.BusinessProcess;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.dwfa.util.io.FileIO;

/**
 * Reads match review item from URL/File
 * 
 * @author Eric Mays (EKM)
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/ace/assignments", type = BeanType.TASK_BEAN) })
public class CreateMatchReviewAssignments extends AbstractTask {

	/**
     *
     */
	private static final long serialVersionUID = 1L;

	private static final int dataVersion = 1;

	private String inputFileNamePropName = ProcessAttachmentKeys.DEFAULT_FILE
			.getAttachmentKey();

	private String bpFileNamePropName = "A: BP_FILE";

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(inputFileNamePropName);
		out.writeObject(bpFileNamePropName);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion >= 1) {
			inputFileNamePropName = (String) in.readObject();
			bpFileNamePropName = (String) in.readObject();
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

			String bpFileName = (String) process
					.readProperty(bpFileNamePropName);
			String inputFileName = (String) process
					.readProperty(inputFileNamePropName);
			File f = new File(inputFileName);
			int i = 0;
			for (File ff : new File(f.getParent()).listFiles()) {
				if (++i == 5)
					break;
				System.out.println("FF: " + ff);
				File bp_file = new File(bpFileName);
				ObjectInputStream ois = new ObjectInputStream(
						new BufferedInputStream(new FileInputStream(bp_file)));
				BusinessProcess bp = (BusinessProcess) ois.readObject();
				ois.close();
				BufferedReader br = new BufferedReader(new FileReader(ff));
				String file_str = FileIO.readerToString(br);
				bp
						.writeAttachment(
								ReadMatchReviewItemFromAttachment.KEY_MATCH_REVIEW_CONTENTS,
								file_str);
				String bp_name = ff.getName().replace(".", "_");
				bp.setName(bp_name);
				bp.setSubject(bp_name);
				ObjectOutputStream oos = new ObjectOutputStream(
						new FileOutputStream(f.getParent() + "/" + bp_name
								+ ".bp"));
				oos.writeObject(bp);
				oos.close();
			}

			return Condition.CONTINUE;
		} catch (Exception e) {
			throw new TaskFailedException(e);
		}
	}

	public int[] getDataContainerIds() {
		return new int[] {};
	}

	public Collection<Condition> getConditions() {
		return AbstractTask.CONTINUE_CONDITION;
	}

	public String getInputFileNamePropName() {
		return inputFileNamePropName;
	}

	public void setInputFileNamePropName(String fileName) {
		this.inputFileNamePropName = fileName;
	}

	public String getBpFileNamePropName() {
		return bpFileNamePropName;
	}

	public void setBpFileNamePropName(String fileName) {
		this.bpFileNamePropName = fileName;
	}

}
