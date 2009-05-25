package org.dwfa.ace.task.refset.members.export;

import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;

public interface RefsetWriter {

    void write(I_ThinExtByRefVersioned refset) throws Exception;

    void closeFiles() throws Exception;
}
