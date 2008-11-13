package org.dwfa.mojo.relformat.xml;

import com.thoughtworks.xstream.XStream;
import org.dwfa.mojo.relformat.exception.ExportDDLMojoException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public final class ReleaseConfigReaderImpl implements ReleaseConfigReader {

    private final XStream xStream;

    public ReleaseConfigReaderImpl() {
        xStream = new XStream();
    }

    public ReleaseConfig reader(final InputStream in) {
        xStream.processAnnotations(ReleaseConfig.class);
        ReleaseConfig config = (ReleaseConfig) xStream.fromXML(in);
        closeStream(in);

        return config;
    }

    public ReleaseConfig reader(final File file) {
        try {
            return reader(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new ExportDDLMojoException(e);
        }
    }

    private void closeStream(final InputStream in) {
        try {
            in.close();
        } catch (IOException e) {
            throw new ExportDDLMojoException(e);
        }
    }
}
