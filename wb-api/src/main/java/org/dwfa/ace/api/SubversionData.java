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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class SubversionData implements Serializable {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private String repositoryUrlStr;
    private String workingCopyStr;
    private String username;
    private String password;
    private Set<String> readOnlyUrlMirrors = null;
    private String preferredReadRepository = null;
    
    private void fixDataErrors() {
    	if (workingCopyStr != null && workingCopyStr.contains("\\")) {
    		workingCopyStr = workingCopyStr.replace('\\', '/');
    	}
        if (repositoryUrlStr != null && repositoryUrlStr.startsWith("scm:svn:")) {
        	repositoryUrlStr = repositoryUrlStr.substring("scm:svn:".length());
        }
    }

    public String getPreferredReadRepository() {
    	fixDataErrors();
        if (preferredReadRepository == null) {
            if (readOnlyUrlMirrors == null || readOnlyUrlMirrors.size() == 0) {
                return repositoryUrlStr;
            }
            preferredReadRepository = readOnlyUrlMirrors.iterator().next();
        }
        return preferredReadRepository;
    }

    public void setPreferredReadRepository(String preferredReadRepository) {
        this.preferredReadRepository = preferredReadRepository;
        getReadOnlyUrlMirrors().add(preferredReadRepository);
    }

    public SubversionData() {
        super();
    }

    public SubversionData(String repositoryUrlStr, String workingCopyStr) {
        super();
        this.repositoryUrlStr = repositoryUrlStr;
        this.workingCopyStr = workingCopyStr;
        this.preferredReadRepository = repositoryUrlStr;
        fixDataErrors();
        getReadOnlyUrlMirrors().add(repositoryUrlStr);
    }

    public Set<String> getReadOnlyUrlMirrors() {
        if (readOnlyUrlMirrors == null) {
            readOnlyUrlMirrors = new HashSet<String>();
            readOnlyUrlMirrors.add(repositoryUrlStr);
        }
        return readOnlyUrlMirrors;
    }

    public String getRepositoryUrlStr() {
        return repositoryUrlStr;
    }

    public void setRepositoryUrlStr(String repositoryUrlStr) {
        getReadOnlyUrlMirrors().remove(this.repositoryUrlStr);
        this.repositoryUrlStr = repositoryUrlStr;
        getReadOnlyUrlMirrors().add(repositoryUrlStr);
    }

    public String getWorkingCopyStr() {
    	fixDataErrors();
    	return workingCopyStr;
    }

    public void setWorkingCopyStr(String workingCopyStr) {
        this.workingCopyStr = workingCopyStr;
        fixDataErrors();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		
		buf.append("svd workingCopy: ");
		buf.append(workingCopyStr);
		
		buf.append(" user: ");
		buf.append(username);
		
		buf.append(" url: ");
		buf.append(repositoryUrlStr);
		
		return buf.toString();
	}
}
