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
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class SubversionData implements Serializable {
    @Override
    public boolean equals(Object obj) {
        boolean result = false;
        if (obj != null && obj instanceof SubversionData) {
            SubversionData object = (SubversionData) obj;
            Method equals;
            Method containsAll;
            try {
                equals = Object.class.getMethod("equals", Object.class);
                containsAll = Set.class.getMethod("containsAll", Set.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if ((object.password.equals(this.password)
                && executeHandleNull(object.preferredReadRepository, this.preferredReadRepository, equals)
                && executeHandleNull(object.repositoryUrlStr, this.repositoryUrlStr, equals)
                && executeHandleNull(object.username, this.username, equals)
                && executeHandleNull(object.workingCopyStr, this.workingCopyStr, equals)
                && executeHandleNull(object.readOnlyUrlMirrors, this.readOnlyUrlMirrors, containsAll) && executeHandleNull(
                this.readOnlyUrlMirrors, object.readOnlyUrlMirrors, containsAll))) {

                result = true;
            }
        }

        return result;
    }

    private boolean executeHandleNull(Object o1, Object o2, Method m) {
        boolean result = false;
        if (o1 == null && o2 == null) {
            result = true;
        } else if ((o1 == null && o2 != null) || (o1 != null && o2 == null)) {
            result = false;
        } else { // both not null
            try {
                result = (Boolean) m.invoke(o1, o2);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

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

    public String getPreferredReadRepository() {
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
        return workingCopyStr;
    }

    public void setWorkingCopyStr(String workingCopyStr) {
        this.workingCopyStr = workingCopyStr;
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
}
