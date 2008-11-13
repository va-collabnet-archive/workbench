package org.dwfa.mojo.relformat.xml;

import java.io.File;
import java.io.InputStream;

public interface ReleaseConfigReader {

    ReleaseConfig reader(InputStream in);

    ReleaseConfig reader(File file);
}
