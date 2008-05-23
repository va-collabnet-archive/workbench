package org.dwfa.ace.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.dnd.FixedTerminologyTransferable;
import org.dwfa.tapi.impl.UniversalFixedConcept;
import org.dwfa.tapi.impl.UniversalFixedDescription;
import org.dwfa.vodb.ToIoException;
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
				return UniversalFixedConcept.get(ConceptBean.get(tuple.getConceptId()).getUids());
			} catch (IOException e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
		}  else if (flavor.equals(FixedTerminologyTransferable.universalFixedConceptInterfaceFlavor)) {
			try {
				return UniversalFixedConcept.get(ConceptBean.get(tuple.getConceptId()).getUids());
			} catch (IOException e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
		}  else if (flavor.equals(FixedTerminologyTransferable.universalFixedDescFlavor)) {
			try {
				return new UniversalFixedDescription(tuple.getDescVersioned().getUniversal().getDescId(),
						ConceptBean.get(tuple.getStatusId()).getUids(), ConceptBean.get(tuple.getConceptId()).getUids(),
						tuple.getInitialCaseSignificant(), ConceptBean.get(tuple.getTypeId()).getUids(), tuple.getText(),
						tuple.getLang());
			} catch (TerminologyException e) {
				throw new ToIoException(e);
			}
		}  else if (flavor.equals(FixedTerminologyTransferable.universalFixedDescInterfaceFlavor)) {
			try {
				return new UniversalFixedDescription(tuple.getDescVersioned().getUniversal().getDescId(),
						ConceptBean.get(tuple.getStatusId()).getUids(), ConceptBean.get(tuple.getConceptId()).getUids(),
						tuple.getInitialCaseSignificant(), ConceptBean.get(tuple.getTypeId()).getUids(), tuple.getText(),
						tuple.getLang());
			} catch (TerminologyException e) {
				throw new ToIoException(e);
			}
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
				if (AceLog.getAppLog().isLoggable(Level.FINE)) {
					AceLog.getAppLog().fine("DT flavor supported: " + flavor);
				}
				return true;
			}
		}
		if (AceLog.getAppLog().isLoggable(Level.FINE)) {
			AceLog.getAppLog().fine("DT flavor not supported" + flavor);
		}
		return false;
	}
}