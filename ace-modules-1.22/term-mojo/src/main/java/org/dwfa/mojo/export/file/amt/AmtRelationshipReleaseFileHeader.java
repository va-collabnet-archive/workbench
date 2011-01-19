package org.dwfa.mojo.export.file.amt;

import org.dwfa.mojo.file.ReleaseFileRow;

/**
 * {@code AmtRelationshipReleaseFileHeader} is an implementation of {@link ReleaseFileRow} that will return the header
 * for an AMT Relationships Release File.
 * @author Matthew Edwards
 */
public final class AmtRelationshipReleaseFileHeader implements ReleaseFileRow {

    @Override
    public String getOutputRow() {
        return AmtRelationshipReleaseFileRow.HEADER_ROW;
    }
}
