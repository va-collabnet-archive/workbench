/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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

import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.dnd.FixedTerminologyTransferable;
import org.dwfa.tapi.impl.UniversalFixedConcept;
import org.dwfa.tapi.impl.UniversalFixedRel;

// :SNOOWL:ADD:CLASS: --- WHAT USES RelationshipTransferable? WAS THIS PART OF OWL?
public class RelationshipTransferable implements Transferable {

    public static String thinRelTupleType = DataFlavor.javaJVMLocalObjectMimeType + ";class="
            + I_RelTuple.class.getName();
    public static final String thinRelVersionedType = DataFlavor.javaJVMLocalObjectMimeType + ";class="
            + I_RelVersioned.class.getName();
    public DataFlavor thinRelVersionedFlavor;
    public DataFlavor thinRelTupleFlavor;
    private DataFlavor conceptBeanFlavor;
    private I_RelTuple tuple;

    public RelationshipTransferable(I_RelTuple tuple) {
        super();
        this.tuple = tuple;
        try {
            thinRelVersionedFlavor = new DataFlavor(RelationshipTransferable.thinRelVersionedType);
            thinRelTupleFlavor = new DataFlavor(RelationshipTransferable.thinRelTupleType);
            conceptBeanFlavor = new DataFlavor(ConceptTransferable.conceptBeanType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public RelationshipTransferable(I_RelVersioned relationship) {
        super();
        this.tuple = relationship.getFirstTuple();
        try {
            thinRelVersionedFlavor = new DataFlavor(RelationshipTransferable.thinRelVersionedType);
            thinRelTupleFlavor = new DataFlavor(RelationshipTransferable.thinRelTupleType);
            conceptBeanFlavor = new DataFlavor(ConceptTransferable.conceptBeanType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (tuple == null) {
            return null;
        }
        I_TermFactory tf = Terms.get();
        if (flavor.equals(conceptBeanFlavor)) {
            try {
                return tf.getConcept(tuple.getConceptNid());
            } catch (TerminologyException e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
            return null;
        } else if (flavor.equals(thinRelVersionedFlavor)) {
            return tuple.getRelVersioned();
        } else if (flavor.equals(thinRelTupleFlavor)) {
            return tuple;
        } else if (flavor.equals(FixedTerminologyTransferable.universalFixedConceptFlavor)) {
            try {
                return UniversalFixedConcept.get(tf.getConcept(tuple.getConceptNid()).getUids());
            } catch (Exception e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
        } else if (flavor.equals(FixedTerminologyTransferable.universalFixedConceptInterfaceFlavor)) {
            try {
                return UniversalFixedConcept.get(tf.getConcept(tuple.getConceptNid()).getUids());
            } catch (Exception e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
        } else if (flavor.equals(FixedTerminologyTransferable.universalFixedRelFlavor)) {
            try {
                return new UniversalFixedRel(tf.getUids(tuple.getRelId()),
                        tf.getUids(tuple.getC1Id()),
                        tf.getUids(tuple.getTypeNid()),
                        tf.getUids(tuple.getC2Id()),
                        tf.getUids(tuple.getCharacteristicNid()),
                        tf.getUids(tuple.getRefinabilityNid()),
                        tuple.getGroup());
            } catch (TerminologyException e) {
                throw new IOException(e);
            }
        } else if (flavor.equals(FixedTerminologyTransferable.universalFixedRelInterfaceFlavor)) {
            try {
                return new UniversalFixedRel(tf.getUids(tuple.getRelId()),
                        tf.getUids(tuple.getC1Id()),
                        tf.getUids(tuple.getTypeNid()),
                        tf.getUids(tuple.getC2Id()),
                        tf.getUids(tuple.getCharacteristicNid()),
                        tf.getUids(tuple.getRefinabilityNid()),
                        tuple.getGroup());
            } catch (TerminologyException e) {
                throw new IOException(e);
            }
        } else if (flavor.equals(DataFlavor.stringFlavor)) {
            return tuple.toUserString();
        }
        throw new UnsupportedFlavorException(flavor);
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{thinRelVersionedFlavor, thinRelTupleFlavor, conceptBeanFlavor,
                    FixedTerminologyTransferable.universalFixedConceptFlavor,
                    FixedTerminologyTransferable.universalFixedConceptInterfaceFlavor,
                    FixedTerminologyTransferable.universalFixedRelFlavor,
                    FixedTerminologyTransferable.universalFixedRelInterfaceFlavor,
                    DataFlavor.stringFlavor};
    }

    @Override
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
