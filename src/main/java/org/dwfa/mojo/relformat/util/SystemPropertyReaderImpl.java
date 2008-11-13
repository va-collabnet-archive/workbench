package org.dwfa.mojo.relformat.util;

public class SystemPropertyReaderImpl implements SystemPropertyReader {

    public String getLineSeparator() {
        return System.getProperty("line.separator");
    }
}
