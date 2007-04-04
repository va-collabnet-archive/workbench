package org.dwfa.ace.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import org.dwfa.ace.AceLog;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.termviewer.dnd.FixedTerminologyTransferable;
import org.dwfa.vodb.types.ConceptBean;

public class DescriptionTransferable implements Transferable {

	private I_DescriptionTuple tuple;
	
	public DescriptionTransferable(I_DescriptionTuple tuple) {
		super();
		this.tuple = tuple;

	}

	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		if (tuple == null) {
			return null;
		}
		if (flavor.equals(TerminologyTransferHandler.conceptBeanFlavor)) {
			return ConceptBean.get(tuple.getConceptId());
		} else if (flavor.equals(TerminologyTransferHandler.thinDescVersionedFlavor)) {
			return tuple.getDescVersioned();
		} else if (flavor.equals(TerminologyTransferHandler.thinDescTupleFlavor)) {
			return tuple;
		}  else if (flavor.equals(FixedTerminologyTransferable.universalFixedConceptFlavor)) {
			try {
				return ConceptBean.get(tuple.getConceptId()).getConceptAttributes().getLocalFixedConcept();
			} catch (IOException e) {
				AceLog.alertAndLogException(e);
			}
		}  else if (flavor.equals(FixedTerminologyTransferable.universalFixedConceptInterfaceFlavor)) {
			try {
				return ConceptBean.get(tuple.getConceptId()).getConceptAttributes().getLocalFixedConcept();
			} catch (IOException e) {
				AceLog.alertAndLogException(e);
			}
		}  else if (flavor.equals(FixedTerminologyTransferable.universalFixedDescFlavor)) {
			return tuple.getDescVersioned().toLocalFixedDesc();
		}  else if (flavor.equals(FixedTerminologyTransferable.universalFixedDescInterfaceFlavor)) {
			return tuple.getDescVersioned().toLocalFixedDesc();
		} else if (flavor.equals(DataFlavor.stringFlavor)) {
			return tuple.getText();
		}
		throw new UnsupportedFlavorException(flavor);
	}

	public DataFlavor[] getTransferDataFlavors() {
		return TerminologyTransferHandler.getSupportedFlavors();
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		for (DataFlavor f : TerminologyTransferHandler.getSupportedFlavors()) {
			if (f.equals(flavor)) {
				return true;
			}
		}
		return false;
	}
}