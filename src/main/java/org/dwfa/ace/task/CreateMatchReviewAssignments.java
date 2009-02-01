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
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.dwfa.bpa.BusinessProcess;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_RenderMessage;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.dwfa.util.io.FileIO;

/**
 * Reads match review item from URL/File and attaches the components to the
 * business process
 * 
 * @author Eric Mays (EKM)
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/assignments", type = BeanType.TASK_BEAN) })
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
			System.out.println("F:" + new File("./").getAbsolutePath());
			String out_dir = "../../src/main/profiles/aao_inbox/";
			String bpFileName = (String) process
					.readProperty(bpFileNamePropName);
			String inputFileName = (String) process
					.readProperty(inputFileNamePropName);
			File f = new File(inputFileName);
			int i = 0;
			for (File ff : new File(f.getParent()).listFiles()) {
				if (!ff.toString().endsWith(".txt"))
					continue;
				// if (++i == 5)
				// break;
				System.out.println("FF: " + ff);
				File bp_file = new File(bpFileName);
				ObjectInputStream ois = new ObjectInputStream(
						new BufferedInputStream(new FileInputStream(bp_file)));
				BusinessProcess bp = (BusinessProcess) ois.readObject();
				ois.close();
				MatchReviewItem mri = new MatchReviewItem();
				mri.createFromFile(ff.getPath());
				bp.writeAttachment(MatchReviewItem.AttachmentKeys.TERM
						.getAttachmentKey(), mri.getTerm());
				bp.writeAttachment(
						MatchReviewItem.AttachmentKeys.UUID_LIST_LIST
								.getAttachmentKey(), mri.getUuidListList());
				bp.writeAttachment(MatchReviewItem.AttachmentKeys.HTML_DETAIL
						.getAttachmentKey(), mri.getHtml());
				bp.writeAttachment(MatchReviewItem.AttachmentKeys.HTML_DETAIL
						.toString(), mri.getHtml());
				String bp_name = ff.getName().replace(".txt", "");
				bp.setName("Review Match");
				String bp_id = bp_name.replace("tm", "");
				while (bp_id.length() < 4) {
					bp_id = "0" + bp_id;
				}
				bp.setSubject(bp_id + ": " + mri.getTerm());
				ObjectOutputStream oos = new ObjectOutputStream(
						new FileOutputStream(out_dir + bp_name + ".bp"));
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
