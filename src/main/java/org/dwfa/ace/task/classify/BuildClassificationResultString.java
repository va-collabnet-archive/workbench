package org.dwfa.ace.task.classify;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.dwfa.ace.api.I_GetConceptData;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;

import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * @author Ming Zhang
 * 
 * Place of issue tracking: need to be specified.
 */

@BeanList(specs = { @Spec(directory = "tasks/ace/classify", type = BeanType.TASK_BEAN) })
public class BuildClassificationResultString extends AbstractTask {

	private static final long serialVersionUID = 1L;

	private static final int dataVersion = 1;

	private String stringName = ProcessKey.ClassificationResultString
			.getAttachmentKey();

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(stringName);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == dataVersion) {
			stringName = (String) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}

	}

	public void complete(I_EncodeBusinessProcess arg0, I_Work arg1)
			throws TaskFailedException {
		// TODO Auto-generated method stub
		// Nothing to do...
	}

	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {

		try {
			final StringBuilder sb = new StringBuilder();
			sb.append("<tr><th>").append("CONCEPT 1").append("</th><th>")
					.append("RELATIONSHIPS").append("</th><th>").append(
							"CONCEPT 2").append("</th><th>").append("GROUP")
					.append("</th></tr>\n");

			I_SnorocketFactory rocket = (I_SnorocketFactory) process
					.readAttachement(ProcessKey.SNOROCKET.getAttachmentKey());

			rocket.getResults(new ProcessResults(worker, sb));

			worker.getLogger().info(sb.toString());

			String returnString = "<html><table align=\"center\" border=\"1\">"
					+ sb.toString() + "</table></html>";
			process.setProperty(stringName, returnString);
			return Condition.CONTINUE;
		} catch (IllegalArgumentException e) {
			throw new TaskFailedException(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TerminologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public Collection<Condition> getConditions() {
		return CONTINUE_CONDITION;
	}

	public int[] getDataContainerIds() {
		return new int[] {};
	}

	private class ProcessResults implements I_SnorocketFactory.I_Callback {

		final private I_Work worker;
		final private StringBuilder sb;

		private I_TermFactory termFactory = LocalVersionedTerminology.get();
		public I_GetConceptData relCharacteristic;
		public I_GetConceptData relRefinability;
		public I_GetConceptData relStatus;
		private int returnedRelCount = 0;

		public ProcessResults(final I_Work worker, StringBuilder sb)
				throws Exception {
			this.worker = worker;
			this.sb = sb;

			relCharacteristic = termFactory
					.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC
							.getUids());
			relRefinability = termFactory
					.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE
							.getUids());
			relStatus = termFactory
					.getConcept(ArchitectonicAuxiliary.Concept.CURRENT
							.getUids());

			worker.getLogger().info(
					"Inferred id is " + relCharacteristic.getConceptId());
			worker.getLogger().info(
					"Inferred UUIDs are " + relCharacteristic.getUids());
			worker.getLogger().info("Inferred concept is " + relCharacteristic);
		}

		public void addRelationship(int conceptId1, int roleId, int conceptId2,
				int group) {
			try {
				returnedRelCount++;
				final I_GetConceptData relSource = termFactory
						.getConcept(conceptId1);
				final I_GetConceptData relType = termFactory.getConcept(roleId);
				final I_GetConceptData relDestination = termFactory
						.getConcept(conceptId2);
				// worker.getLogger().info(relSource + " " + relType + " " +
				// relDestination + " " + group);

				sb.append("<tr><td>").append(relSource).append("</td><td>")
						.append(relType).append("</td><td>").append(
								relDestination).append("</td><td>").append(
								group).append("</td></tr>\n");
			} catch (TerminologyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public String getStringName() {
		return stringName;
	}

	public void setStringName(String stringName) {
		this.stringName = stringName;
	}

}
