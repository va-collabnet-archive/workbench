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
package org.dwfa.ace.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.logging.Level;

import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.dnd.FixedTerminologyTransferable;
import org.dwfa.tapi.impl.UniversalFixedConcept;
import org.dwfa.tapi.impl.UniversalFixedDescription;

public class DescriptionTransferable implements Transferable {

    public static String thinDescTupleType = DataFlavor.javaJVMLocalObjectMimeType + ";class="
        + I_DescriptionTuple.class.getName();

    public static final String thinDescVersionedType = DataFlavor.javaJVMLocalObjectMimeType + ";class="
        + I_DescriptionVersioned.class.getName();

    public DataFlavor thinDescVersionedFlavor;
    public DataFlavor thinDescTupleFlavor;
    private DataFlavor conceptBeanFlavor;
    private I_DescriptionTuple tuple;

    public DescriptionTransferable(I_DescriptionTuple tuple) {
        super();
        this.tuple = tuple;
        try {
            thinDescVersionedFlavor = new DataFlavor(DescriptionTransferable.thinDescVersionedType);
            thinDescTupleFlavor = new DataFlavor(DescriptionTransferable.thinDescTupleType);
            conceptBeanFlavor = new DataFlavor(ConceptTransferable.conceptBeanType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public DescriptionTransferable(I_DescriptionVersioned description) {
        super();
        this.tuple = description.getFirstTuple();
        try {
            thinDescVersionedFlavor = new DataFlavor(DescriptionTransferable.thinDescVersionedType);
            thinDescTupleFlavor = new DataFlavor(DescriptionTransferable.thinDescTupleType);
            conceptBeanFlavor = new DataFlavor(ConceptTransferable.conceptBeanType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (tuple == null) {
            return null;
        }
        I_TermFactory tf = Terms.get();
        if (flavor.equals(conceptBeanFlavor)) {
            try {
                return tf.getConcept(tuple.getConceptId());
            } catch (TerminologyException e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
            return null;
        } else if (flavor.equals(thinDescVersionedFlavor)) {
            return tuple.getDescVersioned();
        } else if (flavor.equals(thinDescTupleFlavor)) {
            return tuple;
        } else if (flavor.equals(FixedTerminologyTransferable.universalFixedConceptFlavor)) {
            try {
                return UniversalFixedConcept.get(tf.getConcept(tuple.getConceptId()).getUids());
            } catch (Exception e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
        } else if (flavor.equals(FixedTerminologyTransferable.universalFixedConceptInterfaceFlavor)) {
            try {
                return UniversalFixedConcept.get(tf.getConcept(tuple.getConceptId()).getUids());
            } catch (Exception e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
        } else if (flavor.equals(FixedTerminologyTransferable.universalFixedDescFlavor)) {
            try {
                return new UniversalFixedDescription(tuple.getDescVersioned().getUniversal().getDescId(),
                    tf.getConcept(tuple.getStatusId()).getUids(), tf.getConcept(tuple.getConceptId()).getUids(),
                    tuple.isInitialCaseSignificant(), tf.getConcept(tuple.getTypeId()).getUids(), tuple.getText(),
                    tuple.getLang());
            } catch (TerminologyException e) {
                throw new IOException(e);
            }
        } else if (flavor.equals(FixedTerminologyTransferable.universalFixedDescInterfaceFlavor)) {
            try {
                return new UniversalFixedDescription(tuple.getDescVersioned().getUniversal().getDescId(),
                    tf.getConcept(tuple.getStatusId()).getUids(), tf.getConcept(tuple.getConceptId()).getUids(),
                    tuple.isInitialCaseSignificant(), tf.getConcept(tuple.getTypeId()).getUids(), tuple.getText(),
                    tuple.getLang());
            } catch (TerminologyException e) {
                throw new IOException(e);
            }
        } else if (flavor.equals(DataFlavor.stringFlavor)) {
            return tuple.getText();
        }
        throw new UnsupportedFlavorException(flavor);
    }

    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] { thinDescVersionedFlavor, thinDescTupleFlavor, conceptBeanFlavor,
                                 FixedTerminologyTransferable.universalFixedConceptFlavor,
                                 FixedTerminologyTransferable.universalFixedConceptInterfaceFlavor,
                                 FixedTerminologyTransferable.universalFixedDescFlavor,
                                 FixedTerminologyTransferable.universalFixedDescInterfaceFlavor,
                                 DataFlavor.stringFlavor };
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        for (DataFlavor f : getTransferDataFlavors()) {
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
