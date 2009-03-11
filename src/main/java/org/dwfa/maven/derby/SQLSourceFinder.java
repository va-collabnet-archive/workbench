package org.dwfa.maven.derby;

import java.io.File;

public interface SQLSourceFinder {

    File[] find(File targetDir, String[] sources, String[] sqlLocations);
}
