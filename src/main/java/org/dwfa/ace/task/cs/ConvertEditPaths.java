package org.dwfa.ace.task.cs;

import java.beans.IntrospectionException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.utypes.cs.I_ProcessUniversalChangeSets;
import org.dwfa.ace.utypes.cs.MapEditPathsProcessor;
import org.dwfa.ace.utypes.cs.UniversalChangeSetReader;
import org.dwfa.ace.utypes.cs.WriteProcessedUniversalChangeSets;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ace/change sets", type = BeanType.TASK_BEAN) })
public class ConvertEditPaths extends AbstractTask {

	private String inputFilePropName = "A: INPUT_FILE";
	private String outputFilePropName = "A: OUTPUT_FILE";
	private String conceptMapPropName = ProcessAttachmentKeys.CON_CON_MAP
			.getAttachmentKey();

	private static final long serialVersionUID = 1;

	private static final int dataVersion = 0;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(inputFilePropName);
		out.writeObject(outputFilePropName);
		out.writeObject(conceptMapPropName);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion <= dataVersion) {
			inputFilePropName = (String) in.readObject();
			outputFilePropName = (String) in.readObject();
			conceptMapPropName = (String) in.readObject();
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
		try {
			String inputFileName = (String) process
					.readProperty(inputFilePropName);
			File inputFile = new File(inputFileName);
			if (!inputFile.exists() || !inputFile.canRead()) {
				throw new TaskFailedException("Specified file '" + inputFileName
						+ "' either does not exist or cannot be read");
			}
			String outputFileName = (String) process.readProperty(outputFilePropName);
			File outputFile = new File(outputFileName);
			
			List<I_ProcessUniversalChangeSets> processors = new ArrayList<I_ProcessUniversalChangeSets>();
			Map<I_GetConceptData, I_GetConceptData> conceptMap = 
				(Map<I_GetConceptData, I_GetConceptData>) process.readProperty(conceptMapPropName);

			processors.add(new MapEditPathsProcessor(conceptMap));
			
			WriteProcessedUniversalChangeSets writer = new WriteProcessedUniversalChangeSets(outputFile);
			processors.add(writer);
			
			UniversalChangeSetReader csr = new UniversalChangeSetReader(
					processors, inputFile);
			csr.read();
			writer.close();
		} catch (IllegalArgumentException e1) {
			throw new TaskFailedException(e1);
		} catch (IntrospectionException e1) {
			throw new TaskFailedException(e1);
		} catch (IllegalAccessException e1) {
			throw new TaskFailedException(e1);
		} catch (InvocationTargetException e1) {
			throw new TaskFailedException(e1);
		} catch (IOException e1) {
			throw new TaskFailedException(e1);
		} catch (ClassNotFoundException e1) {
			throw new TaskFailedException(e1);
		}
		return Condition.CONTINUE;
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

	public String getInputFilePropName() {
		return inputFilePropName;
	}

	public void setInputFilePropName(String inputFilePropName) {
		this.inputFilePropName = inputFilePropName;
	}

	public String getOutputFilePropName() {
		return outputFilePropName;
	}

	public void setOutputFilePropName(String outputFilePropName) {
		this.outputFilePropName = outputFilePropName;
	}

	public String getConceptMapPropName() {
		return conceptMapPropName;
	}

	public void setConceptMapPropName(String conceptMapPropName) {
		this.conceptMapPropName = conceptMapPropName;
	}
}
