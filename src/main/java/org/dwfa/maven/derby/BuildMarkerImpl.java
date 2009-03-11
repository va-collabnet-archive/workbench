package org.dwfa.maven.derby;

import java.io.File;
import java.io.IOException;

public final class BuildMarkerImpl implements BuildMarker {

    private File goalFileDirectory;
    private File goalFile;

    public BuildMarkerImpl(final String buildHashCode) {
        goalFileDirectory = new File("target" + File.separator + "completed-mojos");
        goalFile = new File(goalFileDirectory, buildHashCode);
    }

    public boolean isMarked() {
        return goalFile.exists();
    }

    public void mark() {
        try {
            goalFileDirectory.mkdirs();
            goalFile.createNewFile();
        } catch (IOException e) {
            throw new BuildMarkerException(e);
        }
    }
}
