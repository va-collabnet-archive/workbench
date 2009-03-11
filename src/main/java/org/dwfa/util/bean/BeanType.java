package org.dwfa.util.bean;

public enum BeanType {
	TASK_BEAN(".task"), GENERIC_BEAN(".bean");
	
	private String suffix;

	private BeanType(String suffix) {
		this.suffix = suffix;
	}

	public String getSuffix() {
		return suffix;
	}
}
