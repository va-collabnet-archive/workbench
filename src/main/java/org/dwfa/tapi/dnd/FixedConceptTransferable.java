package org.dwfa.tapi.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import org.dwfa.tapi.I_ConceptualizeLocally;

public class FixedConceptTransferable extends FixedTerminologyTransferable {

    
    private I_ConceptualizeLocally concept;
    
    private static DataFlavor[] supportedFlavors = new DataFlavor[] {
            universalFixedConceptFlavor,
            universalFixedConceptInterfaceFlavor,
            DataFlavor.stringFlavor
    };
    
    public FixedConceptTransferable(I_ConceptualizeLocally concept) {
        super();
        this.concept = concept;
    }
    
    public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException, IOException {
        if ((universalFixedConceptFlavor.equals(flavor)) ||
                (universalFixedConceptInterfaceFlavor.equals(flavor))){
            try {
                return concept.universalize();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (DataFlavor.stringFlavor.equals(flavor)) {
            try {
                System.out.println(concept);
                return concept.universalize().toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    protected DataFlavor[] getSupportedFlavors() {
        return supportedFlavors;
    }


}
