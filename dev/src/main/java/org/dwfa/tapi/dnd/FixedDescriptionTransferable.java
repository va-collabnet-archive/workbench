package org.dwfa.tapi.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import org.dwfa.tapi.I_DescribeConceptLocally;

public class FixedDescriptionTransferable extends FixedTerminologyTransferable {

    private I_DescribeConceptLocally description;
    
    private static DataFlavor[] supportedFlavors = new DataFlavor[] {
            universalFixedDescFlavor,
            universalFixedDescInterfaceFlavor,
            universalFixedConceptFlavor,
            universalFixedConceptInterfaceFlavor,
            DataFlavor.stringFlavor
    };
    
    public FixedDescriptionTransferable(I_DescribeConceptLocally concept) {
        super();
        this.description = concept;
    }
    
    public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException, IOException {
        if ((universalFixedDescFlavor.equals(flavor)) ||
                (universalFixedDescInterfaceFlavor.equals(flavor))){
            try {
                return description.universalize();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if ((universalFixedConceptFlavor.equals(flavor)) ||
                (universalFixedConceptInterfaceFlavor.equals(flavor))){
            try {
                return description.getConcept().universalize();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (DataFlavor.stringFlavor.equals(flavor)) {
            try {
                return description.universalize().toString();
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
