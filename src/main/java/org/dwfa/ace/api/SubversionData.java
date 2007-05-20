package org.dwfa.ace.api;

import java.io.Serializable;

public class SubversionData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String repositoryUrlStr;
	private String workingCopyStr;
	private String username;
	private String password;
	
	
	public SubversionData(String repositoryUrlStr, String workingCopyStr) {
		super();
		this.repositoryUrlStr = repositoryUrlStr;
		this.workingCopyStr = workingCopyStr;
	}
	
	public SubversionData() {
		super();
	}
	
	public String getRepositoryUrlStr() {
		return repositoryUrlStr;
	}
	public void setRepositoryUrlStr(String repositoryUrlStr) {
		this.repositoryUrlStr = repositoryUrlStr;
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
