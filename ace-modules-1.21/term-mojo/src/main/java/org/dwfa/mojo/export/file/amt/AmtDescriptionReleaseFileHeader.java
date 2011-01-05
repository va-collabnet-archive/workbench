package org.dwfa.mojo.export.file.amt;

import org.dwfa.mojo.file.ReleaseFileRow;

/**
 * {@code AmtDescriptionReleaseFileHeader} is an implementation of {@link ReleaseFileRow} that will return the header
 * for an AMT Descriptions Release File.
 * @author Matthew Edwards
 */
public final class AmtDescriptionReleaseFileHeader  implements ReleaseFileRow {

    @Override
    public String getOutputRow() {
        return AmtDescriptionReleaseFileRow.HEADER_ROW;
    }

}
