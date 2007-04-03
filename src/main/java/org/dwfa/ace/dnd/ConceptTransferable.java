/**
 * 
 */
package org.dwfa.ace.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.logging.Level;

import org.dwfa.ace.AceLog;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.termviewer.dnd.FixedTerminologyTransferable;

import com.sleepycat.je.DatabaseException;

public class ConceptTransferable implements Transferable {

	I_GetConceptData conceptTransferable;
	
	DataFlavor conceptBeanFlavor;

	DataFlavor[] supportedFlavors;

	public ConceptTransferable(I_GetConceptData concept) {
		super();
		this.conceptTransferable = concept;

		try {
			conceptBeanFlavor = new DataFlavor(TerminologyTransferHandler.conceptBeanType);
		} catch (ClassNotFoundException e) {
			// should never happen.
			throw new RuntimeException(e);
		}
		supportedFlavors = new DataFlavor[] { conceptBeanFlavor,
				FixedTerminologyTransferable.universalFixedConceptFlavor,
				FixedTerminologyTransferable.universalFixedConceptInterfaceFlavor,
				DataFlavor.stringFlavor };
	}

	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		if (conceptTransferable == null) {
			return null;
		}
		if (flavor.equals(conceptBeanFlavor)) {
			return conceptTransferable;
		} else if (flavor.equals(FixedTerminologyTransferable.universalFixedConceptFlavor)) {
			try {
				return conceptTransferable.getConceptAttributes().getLocalFixedConcept().universalize();
			} catch (DatabaseException e) {
				AceLog.log(Level.SEVERE, e.getLocalizedMessage(), e);
			} catch (TerminologyException e) {
				AceLog.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		} else if (flavor.equals(FixedTerminologyTransferable.universalFixedConceptInterfaceFlavor)) {
			try {
				return conceptTransferable.getConceptAttributes().getLocalFixedConcept().universalize();
			} catch (DatabaseException e) {
				AceLog.log(Level.SEVERE, e.getLocalizedMessage(), e);
			} catch (TerminologyException e) {
				AceLog.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		} else if (flavor.equals(DataFlavor.stringFlavor)) {
			return conceptTransferable.toString();
		}
		throw new UnsupportedFlavorException(flavor);
	}

	public DataFlavor[] getTransferDataFlavors() {
		return supportedFlavors;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		for (DataFlavor f : supportedFlavors) {
			if (f.equals(flavor)) {
				return true;
			}
		}
		return false;
	}
}