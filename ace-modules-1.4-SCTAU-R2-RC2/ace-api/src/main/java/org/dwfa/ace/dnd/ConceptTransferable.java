/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * 
 */
package org.dwfa.ace.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.logging.Level;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.dnd.FixedTerminologyTransferable;

public class ConceptTransferable implements Transferable {

    public static String conceptBeanType = DataFlavor.javaJVMLocalObjectMimeType + ";class="
        + I_GetConceptData.class.getName();

    I_GetConceptData conceptTransferable;

    private DataFlavor conceptBeanFlavor;

    DataFlavor[] supportedFlavors;

    public ConceptTransferable(I_GetConceptData concept) {
        super();
        this.conceptTransferable = concept;

        try {
            conceptBeanFlavor = new DataFlavor(conceptBeanType);
        } catch (ClassNotFoundException e) {
            // should never happen.
            throw new RuntimeException(e);
        }
        supportedFlavors = new DataFlavor[] { conceptBeanFlavor,
                                             FixedTerminologyTransferable.universalFixedConceptFlavor,
                                             FixedTerminologyTransferable.universalFixedConceptInterfaceFlavor,
                                             DataFlavor.stringFlavor };
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (conceptTransferable == null) {
            return null;
        }
        if (flavor.equals(conceptBeanFlavor)) {
            return conceptTransferable;
        } else if (flavor.equals(FixedTerminologyTransferable.universalFixedConceptFlavor)) {
            try {
                return conceptTransferable.getConceptAttributes().getLocalFixedConcept().universalize();
            } catch (IOException e) {
                AceLog.getAppLog().log(Level.SEVERE, e.getLocalizedMessage(), e);
            } catch (TerminologyException e) {
                AceLog.getAppLog().log(Level.SEVERE, e.getLocalizedMessage(), e);
            }
        } else if (flavor.equals(FixedTerminologyTransferable.universalFixedConceptInterfaceFlavor)) {
            try {
                return conceptTransferable.getConceptAttributes().getLocalFixedConcept().universalize();
            } catch (IOException e) {
                AceLog.getAppLog().log(Level.SEVERE, e.getLocalizedMessage(), e);
            } catch (TerminologyException e) {
                AceLog.getAppLog().log(Level.SEVERE, e.getLocalizedMessage(), e);
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
