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
package org.dwfa.tapi.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import org.dwfa.tapi.I_ConceptualizeLocally;

public class FixedConceptTransferable extends FixedTerminologyTransferable {

    private I_ConceptualizeLocally concept;

    private static DataFlavor[] supportedFlavors = new DataFlavor[] { universalFixedConceptFlavor,
                                                                     universalFixedConceptInterfaceFlavor,
                                                                     DataFlavor.stringFlavor };

    public FixedConceptTransferable(I_ConceptualizeLocally concept) {
        super();
        this.concept = concept;
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if ((universalFixedConceptFlavor.equals(flavor)) || (universalFixedConceptInterfaceFlavor.equals(flavor))) {
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
