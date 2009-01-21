package org.dwfa.ace.api;

import java.io.File;
import java.util.Collection;
import java.util.List;

public interface I_ConfigAceDb {
    public List<I_ConfigAceFrame> getAceFrames();
    
    public String getAceRiverConfigFile();
	public void setAceRiverConfigFile(String aceRiverConfigFile);
	
    public Long getCacheSize();
    public void setCacheSize(Long cacheSize);
    
    public File getDbFolder();
    public void setDbFolder(File dbFolder);

    public File getChangeSetRoot();
    public void setChangeSetRoot(File changeSetRoot);

    public String getChangeSetWriterFileName();
    public void setChangeSetWriterFileName(String changeSetWriterFileName);
    
	public String getLoggerRiverConfigFile();
	public void setLoggerRiverConfigFile(String loggerConfigFile);

    public File getProfileFile();
    public void setProfileFile(File profileFile);
    
    public Collection<String> getQueues();
    
	public String getUsername();
	public void setUsername(String username);

}
