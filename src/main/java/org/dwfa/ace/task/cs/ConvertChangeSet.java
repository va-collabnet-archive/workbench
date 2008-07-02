package org.dwfa.ace.task.cs;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.logging.Logger;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * @author Dion McMurtrie
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/ace/change sets", type = BeanType.TASK_BEAN) })
public class ConvertChangeSet extends AbstractTask {

	protected String outputSuffix = ".xml";

	protected String filename = "";

	protected String changeSetTransformer = "org.dwfa.ace.task.cs.ChangeSetXmlEncoder";

	private static final long serialVersionUID = 1;

	private static final int dataVersion = 0;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(filename);
		out.writeObject(outputSuffix);
		out.writeObject(changeSetTransformer);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion <= dataVersion) {
			filename = (String) in.readObject();
			outputSuffix = (String) in.readObject();
			changeSetTransformer = (String) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}

	}

	/**
	 * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
	 *      org.dwfa.bpa.process.I_Work)
	 */
	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {

		Logger logger = worker.getLogger();

		File file = new File(filename);

		if (!file.exists() || !file.canRead()) {
			throw new TaskFailedException("Specified file '" + filename
					+ "' either does not exist or cannot be read");
		}

		ChangeSetTransformer encoder = getChangeSetTransformer(changeSetTransformer);
		encoder.setOutputSuffix(outputSuffix);
		
		try {
			encoder.createXmlCopy(logger, file);
		} catch (Exception e) {
			throw new TaskFailedException("Failed processing file " + file, e);
		}

		return Condition.CONTINUE;
	}

	public static ChangeSetTransformer getChangeSetTransformer(String changeSetTransformer) throws TaskFailedException {
		try {
			return (ChangeSetTransformer) Class.forName(changeSetTransformer).newInstance();
		} catch (Exception e) {
			throw new TaskFailedException("Failed to get transformer class " + changeSetTransformer, e);
		}
	}

	/**
	 * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
	 *      org.dwfa.bpa.process.I_Work)
	 */
	public void complete(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		// Nothing to do.

	}

	/**
	 * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
	 */
	public Collection<Condition> getConditions() {
		return CONTINUE_CONDITION;
	}

	/**
	 * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
	 */
	public int[] getDataContainerIds() {
		return new int[] {};
	}

	public String getOutputSuffix() {
		return outputSuffix;
	}

	public void setOutputSuffix(String outputSuffix) {
		this.outputSuffix = outputSuffix;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public void setChangeSetTransformer(String changeSetTransformer) {
		this.changeSetTransformer = changeSetTransformer;
	}

	public String getChangeSetTransformer() {
		return changeSetTransformer;
	}

}
