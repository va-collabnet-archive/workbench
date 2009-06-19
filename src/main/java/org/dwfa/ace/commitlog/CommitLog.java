package org.dwfa.ace.commitlog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.I_Transact;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.cs.I_WriteChangeSet;
import org.dwfa.ace.api.ebr.I_GetExtensionData;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.utypes.UniversalIdList;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.io.FileIO;

public class CommitLog implements I_WriteChangeSet {

	   /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private File changeSetFile;

	   private File tempFile;

	   private transient OutputStreamWriter tempOut;
	   
	   private transient I_TermFactory tf;
	   
	   private static SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss");

	   public CommitLog(File changeSetFile, File tempFile) {
		super();
		this.changeSetFile = changeSetFile;
		this.tempFile = tempFile;
	}

	public void commit() throws IOException {
	      if (tempOut != null) {
	          tempOut.flush();
	          tempOut.close();
	          tempOut = null;
		       String canonicalFileString = tempFile.getCanonicalPath();
		       if (tempFile.exists()) {
			       if (tempFile.renameTo(changeSetFile) == false) {
				          AceLog.getAppLog().alertAndLogException(new Exception("tempFile.renameTo failed. Attempting FileIO.copyFile..."));
				          FileIO.copyFile(tempFile.getCanonicalPath(), changeSetFile.getCanonicalPath());
				   }
			       tempFile = new File(canonicalFileString);
			       tempFile.delete();
		       }
	       }
	   }

	public void open() throws IOException {
	      if (changeSetFile.exists() == false) {
	          changeSetFile.getParentFile().mkdirs();
	          changeSetFile.createNewFile();
	       }
	       FileIO.copyFile(changeSetFile.getCanonicalPath(), tempFile.getCanonicalPath());
	       tempOut = new OutputStreamWriter(new FileOutputStream(tempFile, true));
	       tf = LocalVersionedTerminology.get();
	}

	public void writeChanges(I_Transact change, long time) throws IOException {
	      try {
			if (I_GetConceptData.class.isAssignableFrom(change.getClass())) {
				  I_GetConceptData conceptChange = (I_GetConceptData) change;
			      tempOut.append(dateFormat.format(new Date(time)));
			      tempOut.append("\tconcept\t");
			      tempOut.append(conceptChange.getInitialText());
			      tempOut.append("\t");
			      tempOut.append(conceptChange.getUids().toString());
			      tempOut.append("\n");
			  } else if (I_Path.class.isAssignableFrom(change.getClass())) {
				  I_Path pathChange = (I_Path) change;
			      tempOut.append(dateFormat.format(new Date(time)));
			      tempOut.append("\tpath\t");
			      tempOut.append(pathChange.toString());
			      tempOut.append(tf.getUids(pathChange.getConceptId()).toString());
			      tempOut.append("\n");
			  } else if (UniversalIdList.class.isAssignableFrom(change.getClass())) {
				  //Nothing to do;
			  } else if (I_GetExtensionData.class.isAssignableFrom(change.getClass())) {
				  I_GetExtensionData extensionChange = (I_GetExtensionData) change;
			      tempOut.append(dateFormat.format(new Date(time)));
			      tempOut.append("\textension\t");
			      tempOut.append(extensionChange.getUniversalAceBean().getMemberUid().toString());
			      tempOut.append("\t");
			      tempOut.append(tf.getConcept(extensionChange.getMemberId()).getInitialText());
			      tempOut.append("\t");
			      tempOut.append(tf.getConcept(extensionChange.getMemberId()).getUids().toString());
			      tempOut.append("\n");
			  } else {
			     throw new IOException("Can't handle class: " + change.getClass().getName());
			  }
		} catch (TerminologyException e) {
			IOException ioe = new IOException(e.getLocalizedMessage());
			ioe.initCause(e);
			throw ioe;
		}
	}

}
