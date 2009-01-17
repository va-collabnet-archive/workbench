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
