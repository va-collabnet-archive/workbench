package org.kp.epic.edg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.dwfa.util.id.Type3UuidFactory;

@BeanList(specs = { @Spec(directory = "tasks/kp/edg", type = BeanType.TASK_BEAN) })
public class LoadEDG extends AbstractTask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int dataVersion = 1;

	private String inputFilePropName = "A: INPUT_FILE";

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(inputFilePropName);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == dataVersion) {
			inputFilePropName = (String) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
	}

	public void complete(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		// Nothing to do
	}

	private static final int SNOMED_ID_FIELD = 0;
	private static final int DESCRIPTION_FIELD = 1;
	private static final int DESCRIPTION_TYPE_FIELD = 2;
	private static final int REFSET_NAME_FIELD = 3;
	private static final int DESCRIPTION_STATUS_FIELD = 4;
	private static final int REFSET_REASON_FOR_SOFT_DELETE_FIELD = 5;
	private static final int DESCRIPTION_EXTERNAL_CLINICAL_CSMID_FIELD = 6;
	private static final int DESCRIPTION_EXTERNAL_DOT1_FIELD = 7;
	private static final int DESCRIPTION_EXTERNAL_ITEM_11_FIELD = 8;
	private static final int DESCRIPTION_EXTERNAL_ITEM_40_FIELD = 9;
	private static final int DESCRIPTION_EXTERNAL_ITEM_100_FIELD = 10;
	private static final int DESCRIPTION_EXTERNAL_ITEM_200_FIELD = 11;
	private static final int DESCRIPTION_EXTERNAL_ITEM_207_FIELD = 12;
	private static final int DESCRIPTION_EXTERNAL_ITEM_2000_FIELD = 13;
	private static final int DESCRIPTION_EXTERNAL_ITEM_7010_FIELD = 14;
	
    int exactMatches = 0;
    int caseInsensitiveMatches = 0;
    int count = 0;

	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		try {
			String inputFileName = (String) process
					.readProperty(inputFilePropName);
			File inputFile = new File(inputFileName);
			if (!inputFile.exists() || !inputFile.canRead()) {
				throw new TaskFailedException("Specified file '"
						+ inputFileName
						+ "' either does not exist or cannot be read");
			}

			I_TermFactory tf = LocalVersionedTerminology.get();
			
		    BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		       String s;
		       reader.readLine(); // skip first line...
		       
		       while ((s=reader.readLine())!=null){
		         String fields[] = s.split("\t");
		         if (fields != null && fields.length > 0) {
			         String snomedCid = fields[SNOMED_ID_FIELD];
			         UUID conceptUuid = Type3UuidFactory.fromSNOMED(snomedCid);
			         count++;
			         checkForDescriptionAndAdd(tf, fields, conceptUuid);
		         }
		       }
		       reader.close();
				 AceLog.getAppLog().info("Processed: " + count + " exactMatches: " + exactMatches + 
						 " caseInsensitiveMatches: " + caseInsensitiveMatches);
		     

			return Condition.CONTINUE;

		} catch (Exception e) {
			throw new TaskFailedException(e);
		}
	}

	private void checkForDescriptionAndAdd(I_TermFactory tf, String[] fields,
			UUID conceptUuid) throws TerminologyException, IOException {
		I_GetConceptData concept = tf.getConcept(new UUID[] { conceptUuid });
		boolean found = false;
		for (I_DescriptionVersioned desc: concept.getDescriptions()) {
			boolean foundExact = false;
			boolean foundCaseInsensitive = false;
			for (I_DescriptionPart dt: desc.getVersions()) {
				if (dt.getText().equals(fields[DESCRIPTION_FIELD])) {
					foundExact = true;
					found = true;
					exactMatches++;
					 AceLog.getAppLog().info("Found exact desc match: " + dt.getText() + " for: " + fields[DESCRIPTION_FIELD]);
					break;
				} else if (dt.getText().toLowerCase().equals(fields[DESCRIPTION_FIELD].toLowerCase())) {
					foundCaseInsensitive = true;
					found = true;
					caseInsensitiveMatches++;
					 AceLog.getAppLog().info("Found case insensitive desc match: " + dt.getText() + " for: " + fields[DESCRIPTION_FIELD]);
					break;
				}
			}
			if (found) {
				break;
			}
		}
	}

	public int[] getDataContainerIds() {
		return new int[] {};
	}

	public Collection<Condition> getConditions() {
		return AbstractTask.CONTINUE_CONDITION;
	}

	public String getInputFilePropName() {
		return inputFilePropName;
	}

	public void setInputFilePropName(String inputFilePropName) {
		this.inputFilePropName = inputFilePropName;
	}
	
}
