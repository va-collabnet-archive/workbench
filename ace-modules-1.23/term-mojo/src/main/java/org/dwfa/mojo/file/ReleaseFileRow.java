package org.dwfa.mojo.file;

/**
 * The {@code ReleaseFileRow} Interface represents any object that should be written in a row by row output file.
 * The implementation determines the contents of the row.
 * @author Matthew Edwards
 */
public interface ReleaseFileRow {

    /**
     * Gets the row information to write to the output file from the implementation.
     * @return a String Representation of the row to write to the output file.
     */
    String getOutputRow();

}
