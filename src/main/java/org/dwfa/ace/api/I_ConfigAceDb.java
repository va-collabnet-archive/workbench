package org.dwfa.ace.api;

import java.io.File;
import java.util.List;

public interface I_ConfigAceDb {
    public List<I_ConfigAceFrame> getAceFrames();
    
    public Long getCacheSize();

    public void setCacheSize(Long cacheSize);
    
    public File getDbFolder();

    public void setDbFolder(File dbFolder);

}
