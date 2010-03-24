package org.dwfa.ace.api;

import java.util.List;

public interface I_AmVersioned<P extends I_AmPart> extends I_AmTermComponent {

    public boolean addVersion(P rel);

    public List<P> getVersions();
}
