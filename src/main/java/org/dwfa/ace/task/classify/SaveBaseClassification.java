package org.dwfa.ace.task.classify;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/classify", type = BeanType.TASK_BEAN) })
public class SaveBaseClassification extends AbstractTask {
	/**
     * 
     */
	private static final long serialVersionUID = 1L;

	private static final int dataVersion = 2;

	/**
	 * Bean property
	 */
	private String fileName = "baseState.txt";

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(getFileName());
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		final int objDataVersion = in.readInt();

		if (objDataVersion > 1) {
			setFileName((String) in.readObject());
		}
		if (objDataVersion > dataVersion) {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
	}

	public void complete(I_EncodeBusinessProcess arg0, I_Work arg1)
			throws TaskFailedException {
		// nothing to do...
	}

	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {

		try {
			LoadClassifyWrite.logMemory("SBC evaluate start", worker);
			I_SnorocketFactory rocket = (I_SnorocketFactory) process
					.readAttachement(ProcessKey.SNOROCKET.getAttachmentKey());

			LoadClassifyWrite.logMemory("SBC pre getClassifierResults", worker);
			getClassifierResults(worker, rocket);
			LoadClassifyWrite
					.logMemory("SBC post getClassifierResults", worker);
			worker.getLogger().info("Finished get results. ");
			// worker.getLogger().info(
			// "Stated and inferred: " + statedAndInferredCount
			// + " stated and subsumbed: "
			// + statedAndSubsumedCount + " inferred count: "
			// + inferredRelCount);

			// EKM - clear this out to reclaim memory
			process
					.writeAttachment(ProcessKey.SNOROCKET.getAttachmentKey(),
							"");
			LoadClassifyWrite.logMemory("SBC evaluate end", worker);
		} catch (Exception e) {
			throw new TaskFailedException(e);
		}

		return Condition.CONTINUE;
	}

	public Collection<Condition> getConditions() {
		return CONTINUE_CONDITION;
	}

	public int[] getDataContainerIds() {
		return new int[] {};
	}

	private void getClassifierResults(I_Work worker, I_SnorocketFactory rocket)
			throws IOException {
		long startTime = System.currentTimeMillis();

		final InputStream is = rocket.getStream();

		final FileOutputStream fos = new FileOutputStream(getFileName());

		final byte[] buffer = new byte[4096];
		int len;
		while ((len = is.read(buffer)) >= 0) {
			fos.write(buffer, 0, len);
		}

		fos.close();

		worker.getLogger().info(
				"Save classification results time: "
						+ (System.currentTimeMillis() - startTime));
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}

}
