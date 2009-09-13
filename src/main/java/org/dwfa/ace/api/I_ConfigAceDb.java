package org.dwfa.ace.api;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface I_ConfigAceDb {
	
    public List<I_ConfigAceFrame> getAceFrames();
    
    public String getAceRiverConfigFile();
	public void setAceRiverConfigFile(String aceRiverConfigFile);
	
	/**
	 * Currently non-functional stub. 
	 * @deprecated
	 */
	public Long getCacheSize();

	/**
	 * Currently non-functional stub. 
	 * @deprecated
	 */
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
	
    public Object getProperty(String key) throws IOException;
    public void setProperty(String key, Object value) throws IOException;
    public Map<String, Object> getProperties() throws IOException;

    /**
     * @return The concept that represents the user this profile belongs to.
     */
	public I_GetConceptData getUserConcept();
	
	/**
	 * 
	 * @param userConcept A concept that represents the user this profile belongs to.
	 */
	public void setUserConcept(I_GetConceptData userConcept);

}
