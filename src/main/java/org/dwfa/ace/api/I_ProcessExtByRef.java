package org.dwfa.ace.api;

import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;

public interface I_ProcessExtByRef {
    public void processExtensionByReference(I_ThinExtByRefVersioned extension) throws Exception;
}
