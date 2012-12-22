package org.ihtsdo.translation;

import java.util.Map;

public class DefaultProjectForLanguage {

	private Map<String,String[]> defaultProjects;

	public DefaultProjectForLanguage(Map<String,String[]> defaultProjects) {
		super();
		this.defaultProjects = defaultProjects;
	}

	public Map<String,String[]> getDefaultProjects() {
		return defaultProjects;
	}

	public void setDefaultProjects(Map<String,String[]> defaultProjects) {
		this.defaultProjects = defaultProjects;
	}
}
