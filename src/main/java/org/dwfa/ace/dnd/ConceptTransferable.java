/**
 * 
 */
package org.dwfa.ace.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import org.dwfa.vodb.types.I_GetConceptData;

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
				DataFlavor.stringFlavor };
	}

	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		if (conceptTransferable == null) {
			return null;
		}
		if (flavor.equals(conceptBeanFlavor)) {
			return conceptTransferable;
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