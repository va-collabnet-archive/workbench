package org.dwfa.maven.derby;

import java.io.OutputStream;
import java.io.IOException;

public final class NullOuputStream extends OutputStream {

    public NullOuputStream() {
        //do nothing.
    }

    @Override
    public void write(final byte[] b) throws IOException {
        //do nothing.
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        //do nothing.
    }

    @Override
    public void flush() throws IOException {
        //do nothing.
    }

    @Override
    public void close() throws IOException {
        //do nothing.
    }

    @Override
    public void write(final int b) throws IOException {
        //do nothing.
    }

    @Override
    public String toString() {
        return "[NullOuputStream - Data is not collected in this OutputStream]";        
    }
}
