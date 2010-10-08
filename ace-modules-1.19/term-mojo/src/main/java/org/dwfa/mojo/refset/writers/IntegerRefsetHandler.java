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
package org.dwfa.mojo.refset.writers;

import java.sql.SQLException;
import java.util.UUID;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartInteger;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;

public class IntegerRefsetHandler extends MemberRefsetHandler {
    @Override
    public String formatRefsetLine(I_TermFactory tf, I_ThinExtByRefTuple tuple, boolean sctid, boolean useRf2)
            throws SQLException, ClassNotFoundException, Exception {
        I_ThinExtByRefPartInteger integerPart = (I_ThinExtByRefPartInteger) tuple.getPart();

        return super.formatRefsetLine(tf, tuple, sctid, useRf2) + MemberRefsetHandler.COLUMN_DELIMITER
            + integerPart.getValue();
    }

    @Override
    public String formatRefsetLine(I_TermFactory tf, I_ThinExtByRefPart part, UUID memberUuid, int refsetId,
            int componentId, boolean sctId, boolean useRf2) throws SQLException, ClassNotFoundException, Exception {
        I_ThinExtByRefPartInteger intPart = (I_ThinExtByRefPartInteger) part;

        return super.formatRefsetLine(tf, part, memberUuid, refsetId, componentId, sctId, useRf2)
            + MemberRefsetHandler.COLUMN_DELIMITER + intPart.getValue();
    }

    /**
     * @throws Exception
     * @throws ClassNotFoundException
     * @throws SQLException
     * @see org.dwfa.mojo.refset.writers.MemberRefsetHandler#formatRefsetLineRF2(org.dwfa.ace.api.I_TermFactory,
     *      org.dwfa.ace.api.ebr.I_ThinExtByRefPart, java.lang.Integer, int,
     *      int, boolean, boolean)
     */
    @Override
    public String formatRefsetLineRF2(I_TermFactory tf, I_ThinExtByRefPart part, UUID memberUuid, int refsetNid,
            int componentId, boolean sctId, boolean useRf2, TYPE type) throws SQLException, ClassNotFoundException,
            Exception {
        I_ThinExtByRefPartInteger intPart = (I_ThinExtByRefPartInteger) part;

        return super.formatRefsetLineRF2(tf, part, memberUuid, refsetNid, componentId, sctId, useRf2, type)
            + MemberRefsetHandler.COLUMN_DELIMITER + intPart.getIntValue();
    }

    @Override
    public String getHeaderLine() {
        return super.getHeaderLine() + MemberRefsetHandler.COLUMN_DELIMITER + "INTEGER_VALUE";
    }

    /**
     * @see org.dwfa.mojo.refset.writers.MemberRefsetHandler#getRF2HeaderLine()
     */
    @Override
    public String getRF2HeaderLine() {
        return super.getRF2HeaderLine() + COLUMN_DELIMITER + "valueId";
    }

    @Override
    protected I_ThinExtByRefPart processLine(String line) {
        I_ThinExtByRefPartInteger part;
        try {

            I_ThinExtByRefVersioned versioned = getExtensionVersioned(line, RefsetAuxiliary.Concept.INT_EXTENSION);

            part = getTermFactory().newIntegerExtensionPart();
            setGenericExtensionPartFields(part);

            part.setValue(Integer.parseInt(getNextCurrentRowToken()));

            versioned.addVersion(part);

            if (isTransactional()) {
                getTermFactory().addUncommitted(versioned);
            } else {
                getTermFactory().getDirectInterface().writeExt(versioned);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error occred processing file " + sourceFile, e);
        }

        return part;
    }
}
