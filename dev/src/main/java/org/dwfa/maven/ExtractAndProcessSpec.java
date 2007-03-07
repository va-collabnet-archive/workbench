package org.dwfa.maven;

public class ExtractAndProcessSpec {
	
	public static class SubstutionSpec {
		private String propertyName = null;
		
		private String patternStr = null;

		private String replacementStr = null;
		
		private NULL_ACTION nullAction = NULL_ACTION.MAKE_UUID;
		
		public SubstutionSpec() {
			super();
		}
		public NULL_ACTION getNullAction() {
			return nullAction;
		}
		public String getNullActionStr() {
			return nullAction.toString();
		}
		public void setNullAction(NULL_ACTION nullAction) {
			this.nullAction = nullAction;
		}
		public void setNullActionStr(String nullActionStr) {
			this.nullAction = NULL_ACTION.valueOf(nullActionStr);
		}
		public String getPatternStr() {
			return patternStr;
		}
		public void setPatternStr(String patternStr) {
			this.patternStr = patternStr;
		}
		public String getPropertyName() {
			return propertyName;
		}
		public void setPropertyName(String propertyName) {
			this.propertyName = propertyName;
		}
		public String getReplacementStr() {
			return replacementStr;
		}
		public void setReplacementStr(String replacementStr) {
			this.replacementStr = replacementStr;
		}
		
	}
	
	public enum NULL_ACTION { PROMPT, MAKE_UUID, REPLACE_LITERAL, EMPTY_STRING };
	
	private String filePatternStr;

	private String destDir;

	private SubstutionSpec[] substitutions = new SubstutionSpec[0];

	public ExtractAndProcessSpec() {
		super();
	}


	public String getFilePatternStr() {
		return filePatternStr;
	}

	public void setFilePatternStr(String filePatternStr) {
		this.filePatternStr = filePatternStr;
	}

	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append("filePatternStr: ");
		buff.append(filePatternStr);
		
		buff.append(" patternStr: ");
		buff.append(" substutions: ");
		for (SubstutionSpec s: substitutions) {
			buff.append(s + " ");
		}
		return buff.toString();
	}

	public String getDestDir() {
		return destDir;
	}

	public void setDestDir(String destDir) {
		this.destDir = destDir;
	}


	public SubstutionSpec[] getSubstitutions() {
		return substitutions;
	}


	public void setSubstitutions(SubstutionSpec[] substitutions) {
		this.substitutions = substitutions;
	}
	
}
