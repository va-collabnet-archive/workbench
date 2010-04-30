/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dwfa.ace.api;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.dwfa.ace.api.cs.ChangeSetPolicy;
import org.dwfa.ace.api.cs.ChangeSetWriterThreading;

public interface I_ConfigAceDb {

    public List<I_ConfigAceFrame> getAceFrames();

    public String getAceRiverConfigFile();

    public void setAceRiverConfigFile(String aceRiverConfigFile);

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
     * @param userConcept A concept that represents the user this profile
     *            belongs to.
     */
    public void setUserConcept(I_GetConceptData userConcept);

    /**
     * 
     * The user path can be used to reset a user to their defaults,
     * by setting the edit path to their user path, and by setting the
     * view path to the latest position on their user path.
     * 
     * @return The default path specifically assigned to this user.
     */
    public I_GetConceptData getUserPath();

    /**
     * 
     * @param userPath The default path specifically assigned to this user.
     */
    public void setUserPath(I_GetConceptData userPath);

    /**
     * 
     * @return The user's full name (John Q. Public).
     */
    public String getFullName();

    /**
     * 
     * @param fullName The user's full name (John Q. Public).
     */
    public void setFullName(String fullName);
    
    public void setUserChangesChangeSetPolicy(ChangeSetPolicy policy);
    public void setClassifierChangesChangeSetPolicy(ChangeSetPolicy policy);
    public void setRefsetChangesChangeSetPolicy(ChangeSetPolicy policy);

    public ChangeSetPolicy getUserChangesChangeSetPolicy();
    public ChangeSetPolicy getClassifierChangesChangeSetPolicy();
    public ChangeSetPolicy getRefsetChangesChangeSetPolicy();
    
    public void setChangeSetWriterThreading(ChangeSetWriterThreading threading);
    public ChangeSetWriterThreading getChangeSetWriterThreading();
    
}
