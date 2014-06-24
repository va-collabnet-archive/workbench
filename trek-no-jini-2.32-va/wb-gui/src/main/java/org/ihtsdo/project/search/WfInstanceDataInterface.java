package org.ihtsdo.project.search;


public interface WfInstanceDataInterface {
	public String getAction();

	public String getState();

	public String getModeler();

	public long getTime();

	public String getConcept();

	public String getFsn();

	public int compareTo(WfInstanceDataInterface wfInstanceData);
}
