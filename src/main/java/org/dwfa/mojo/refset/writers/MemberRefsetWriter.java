package org.dwfa.mojo.refset.writers;

import java.io.IOException;
import java.text.SimpleDateFormat;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.tapi.TerminologyException;

public class MemberRefsetWriter {

	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddThhmmss");;

	/**
	 * @return the header line for the refset file of this type
	 */
	public String getHeaderLine() {
		return "ID\tPATH_ID\tEFFECTIVE_DATE\tACTIVE\tCOMPONENT_ID";
	}
	
	/**
	 * @param tuple extension part to format
	 * @return string representation of the part fit for a file of this handler's type
	 * @throws IOException 
	 * @throws TerminologyException 
	 */
	public String formatRefsetLine(I_TermFactory tf, I_ThinExtByRefTuple tuple) throws TerminologyException, IOException {
		return toId(tf, tuple.getRefsetId()) + "\t"
				+ toId(tf, tuple.getPathId()) + "\t"
				+ getDate(tf, tuple.getVersion()) + "\t"
				+ toId(tf, tuple.getStatus()) + "\t"
				+ toId(tf, tuple.getComponentId());
	}

	private String getDate(I_TermFactory tf, int version) {
		return dateFormat.format(tf.convertToThickVersion(version)) + "Z";
	}

	protected String toId(I_TermFactory tf, int componentId) throws TerminologyException, IOException {
		return tf.getUids(componentId).iterator().next().toString();
	}
	
	
}
