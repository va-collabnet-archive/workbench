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
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.I_DescribeConceptLocally;
import org.dwfa.tapi.I_DescribeConceptUniversally;
import org.dwfa.tapi.I_RelateConceptsUniversally;
import org.dwfa.tapi.impl.UniversalFixedDescription;
import org.dwfa.tapi.impl.UniversalFixedRel;

public abstract class FixedTerminologyTransferable implements Transferable {

    public static DataFlavor universalFixedConceptFlavor = new DataFlavor(
        "application/x-java-jvm-local-objectref; class=" + I_ConceptualizeUniversally.class.getName(),
        "Universal Fixed Concept");
    public static DataFlavor universalFixedConceptInterfaceFlavor = new DataFlavor(I_ConceptualizeUniversally.class,
        "Universal Fixed Concept Interface");
    public static DataFlavor universalFixedDescFlavor = new DataFlavor(UniversalFixedDescription.class,
        "Universal Fixed Description");
    public static DataFlavor universalFixedDescInterfaceFlavor = new DataFlavor(
        "application/x-java-jvm-local-objectref; class=" + I_DescribeConceptUniversally.class.getName(),
        "Universal Fixed Description Interface");
    public static DataFlavor universalFixedRelFlavor = new DataFlavor(UniversalFixedRel.class,
        "Universal Fixed Relationship");
    public static DataFlavor universalFixedRelInterfaceFlavor = new DataFlavor(
        "application/x-java-jvm-local-objectref; class=" + I_RelateConceptsUniversally.class.getName(),
        "Universal Fixed Relationship Interface");

    public DataFlavor[] getTransferDataFlavors() {
        return getSupportedFlavors();
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        for (DataFlavor f : getSupportedFlavors()) {
            if (flavor.equals(f)) {
                return true;
            }
        }
        return false;
    }

    protected abstract DataFlavor[] getSupportedFlavors();

    public static Transferable get(Object obj) {
        if (obj != null) {
            if (I_ConceptualizeLocally.class.isAssignableFrom(obj.getClass())) {
                System.out.println("Making FixedConceptTransferable");
                return new FixedConceptTransferable((I_ConceptualizeLocally) obj);
            } else if (I_DescribeConceptLocally.class.isAssignableFrom(obj.getClass())) {
                System.out.println("Making FixedDescriptionTransferable");
                return new FixedDescriptionTransferable((I_DescribeConceptLocally) obj);
            }
            System.out.println("Making StringSelection Transferable");
            return new StringSelection(obj.toString());
        }
        return new StringSelection("null");
    }

}
