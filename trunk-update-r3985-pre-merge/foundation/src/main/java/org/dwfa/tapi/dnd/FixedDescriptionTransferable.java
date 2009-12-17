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

import org.dwfa.tapi.I_DescribeConceptLocally;

public class FixedDescriptionTransferable extends FixedTerminologyTransferable {

    private I_DescribeConceptLocally description;

    private static DataFlavor[] supportedFlavors = new DataFlavor[] { universalFixedDescFlavor,
                                                                     universalFixedDescInterfaceFlavor,
                                                                     universalFixedConceptFlavor,
                                                                     universalFixedConceptInterfaceFlavor,
                                                                     DataFlavor.stringFlavor };

    public FixedDescriptionTransferable(I_DescribeConceptLocally concept) {
        super();
        this.description = concept;
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if ((universalFixedDescFlavor.equals(flavor)) || (universalFixedDescInterfaceFlavor.equals(flavor))) {
            try {
                return description.universalize();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if ((universalFixedConceptFlavor.equals(flavor))
            || (universalFixedConceptInterfaceFlavor.equals(flavor))) {
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
