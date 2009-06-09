package org.dwfa.ace.task;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.binding.java.GenerateClassFromRefset;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide", type = BeanType.TASK_BEAN) })
public class GenerateConceptSpecFromRefset extends AbstractTask {

	private static final long serialVersionUID = 1;

	private static final int dataVersion = 1;

	/**
	 * The Java package name. Property key.
	 */
	private String packageNamePropertyKey = "A: PACKAGE_NAME";

	/**
	 * The Java class name. Property key.
	 */
	private String classNamePropertyKey = "A: CLASS_NAME";

	/**
	 * The Java file output location. Property key.
	 */
	private String outputDirectoryPropertyKey = "A: OUTPUT_DIRECTORY";

	public String getPackageNamePropertyKey() {
		return packageNamePropertyKey;
	}

	public void setPackageNamePropertyKey(String packageName) {
		this.packageNamePropertyKey = packageName;
	}

	public String getClassNamePropertyKey() {
		return classNamePropertyKey;
	}

	public void setClassNamePropertyKey(String className) {
		this.classNamePropertyKey = className;
	}

	public String getOutputDirectoryPropertyKey() {
		return outputDirectoryPropertyKey;
	}

	public void setOutputDirectoryPropertyKey(String outputDirectoryPropertyKey) {
		this.outputDirectoryPropertyKey = outputDirectoryPropertyKey;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(this.packageNamePropertyKey);
		out.writeObject(this.classNamePropertyKey);
		out.writeObject(this.outputDirectoryPropertyKey);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion >= 1) {
			this.packageNamePropertyKey = (String) in.readObject();
			this.classNamePropertyKey = (String) in.readObject();
			this.outputDirectoryPropertyKey = (String) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
	}

	public Condition evaluate(I_EncodeBusinessProcess process,
			final I_Work worker) throws TaskFailedException {
		try {
			String packageName = (String) process
					.readProperty(this.packageNamePropertyKey);
			String className = (String) process
					.readProperty(this.classNamePropertyKey);
			File outputDirectory = (File) process
					.readProperty(this.outputDirectoryPropertyKey);
			I_ConfigAceFrame config = (I_ConfigAceFrame) worker
					.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG
							.name());
			I_GetConceptData con = config.getHierarchySelection();
			if (con != null)
				System.out.println(con.getInitialText());
			System.out.println(packageName + "." + className + ">>"
					+ outputDirectory);
			GenerateClassFromRefset gcfr = new GenerateClassFromRefset();
			gcfr.setPackageName(packageName);
			gcfr.setClassName(className);
			gcfr.setOutputDirectory(outputDirectory);
			gcfr.setRefsetName(con.getInitialText());
			gcfr.setRefsetUuid(con.getUids().get(0).toString());
			gcfr.run();
			return Condition.ITEM_COMPLETE;
		} catch (Exception e) {
			throw new TaskFailedException(e);
		}
	}

	public void complete(I_EncodeBusinessProcess arg0, I_Work arg1)
			throws TaskFailedException {
		// TODO Auto-generated method stub

	}

	public Collection<Condition> getConditions() {
		return AbstractTask.ITEM_CANCELED_OR_COMPLETE;
	}
}
