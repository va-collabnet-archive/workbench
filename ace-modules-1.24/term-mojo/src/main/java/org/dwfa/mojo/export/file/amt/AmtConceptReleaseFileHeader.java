package org.dwfa.mojo.export.file.amt;

import org.dwfa.mojo.file.ReleaseFileRow;

/**
 * {@code AmtConceptReleaseFileHeader} is an implementation of {@link ReleaseFileRow} that will return the header
 * for an AMT Concepts Release File.
 * @author Matthew Edwards
 */
public final class AmtConceptReleaseFileHeader implements ReleaseFileRow {

    @Override
    public String getOutputRow() {
        return AmtConceptReleaseFileRow.HEADER_ROW;
    }
}
