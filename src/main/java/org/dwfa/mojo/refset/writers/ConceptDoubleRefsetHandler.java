package org.dwfa.mojo.refset.writers;

import java.io.IOException;
import java.util.UUID;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartMeasurement;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;

public class ConceptDoubleRefsetHandler extends MemberRefsetHandler {
    @Override
    public String formatRefsetLine(I_TermFactory tf, I_ThinExtByRefTuple tuple, boolean sctid) throws TerminologyException, IOException {
        I_ThinExtByRefPartMeasurement measurementPart = (I_ThinExtByRefPartMeasurement) tuple.getPart();

        return super.formatRefsetLine(tf, tuple, sctid) + MemberRefsetHandler.FILE_DELIMITER
                      + toId(tf, measurementPart.getUnitsOfMeasureId(), sctid) + MemberRefsetHandler.FILE_DELIMITER
                      + measurementPart.getMeasurementValue();
    }

    @Override
    public String getHeaderLine() {
        return super.getHeaderLine() + MemberRefsetHandler.FILE_DELIMITER + "CONCEPT_VALUE" + MemberRefsetHandler.FILE_DELIMITER + "DOUBLE_VALUE";
    }

    @Override
    protected I_ThinExtByRefPart processLine(String line) {
        I_ThinExtByRefPartMeasurement part;
        try {

            I_ThinExtByRefVersioned versioned = getExtensionVersioned(line, RefsetAuxiliary.Concept.MEASUREMENT_EXTENSION);

            part = getTermFactory().newMeasurementExtensionPart();
            setGenericExtensionPartFields(part);

            String conceptValue = getNextCurrentRowToken();
            part.setUnitsOfMeasureId(getNid(UUID.fromString(conceptValue)));

            part.setMeasurementValue(Double.parseDouble(getNextCurrentRowToken()));

            versioned.addVersion(part);

            if (isTransactional()) {
                getTermFactory().addUncommitted(versioned);
            } else {
                getTermFactory().getDirectInterface().writeExt(versioned);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error occurred processing file " + sourceFile, e);
        }

        return part;
    }
}
