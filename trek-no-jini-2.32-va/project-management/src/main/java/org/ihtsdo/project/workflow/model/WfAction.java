/*
 * Copyright (c) 2010 International Health Terminology Standards Development
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
package org.ihtsdo.project.workflow.model;

import java.io.File;
import java.io.Serializable;
import java.util.UUID;

/**
 * The Class WfAction.
 */
public class WfAction implements Serializable {

	public static final String SEND_TO_OUTBOX = "Send to outbox";
	public static final String NO_ACTION = "No action";
	public static final String NEXT_ITEM = "Next item";

	/** The name. */
	private String name;
	
	/** The id. */
	private UUID id;
	
	/** The consequence. */
	private WfState consequence;
	
	/** The business process. */
	private File businessProcess;
	
	/**
	 * Instantiates a new wf action.
	 *
	 * @param name the name
	 */
	public WfAction(String name) {
		super();
		this.name = name;
	}
	
	/**
	 * Instantiates a new wf action.
	 *
	 * @param name the name
	 * @param id the id
	 * @param consequence the consequence
	 * @param businessProcess the business process
	 */
	public WfAction(String name, UUID id, WfState consequence,
			File businessProcess) {
		super();
		this.name = name;
		this.id = id;
		this.consequence = consequence;
		this.businessProcess = businessProcess;
	}

	/**
	 * Instantiates a new wf action.
	 */
	public WfAction() {
		super();
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public UUID getId() {
		return id;
	}

	/**
	 * Sets the id.
	 *
	 * @param id the new id
	 */
	public void setId(UUID id) {
		this.id = id;
	}

	/**
	 * Gets the consequence.
	 *
	 * @return the consequence
	 */
	public WfState getConsequence() {
		return consequence;
	}

	/**
	 * Sets the consequence.
	 *
	 * @param consequence the new consequence
	 */
	public void setConsequence(WfState consequence) {
		this.consequence = consequence;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return name;
	}

	/**
	 * Gets the business process.
	 *
	 * @return the business process
	 */
	public File getBusinessProcess() {
		return correctPathForPlatform(businessProcess);
	}
	
	private File correctPathForPlatform(File file)
	{
		//The WFAction is defined on one computer (say windows) then the path is stored in here like  
		//sampleProcesses\CommentActionWithoutDestination.bp
		//Then, the WFAction is serialized, and synced - say to a linux computer.
		//And it fails.  Must correct the path, if necessary.
		
		String s = file.getName();
		if (File.separatorChar == '/')
		{
			s = s.replaceAll("\\\\", "/");
		}
		else if (File.separatorChar == '\\')
		{
			s = s.replaceAll("/", "\\");
		}
		if (file.getName().equals(s))
		{
			return file;
		}
		else
		{
			return new File(s);
		}
	}

	/**
	 * Sets the business process.
	 *
	 * @param businessProcess the new business process
	 */
	public void setBusinessProcess(File businessProcess) {
		this.businessProcess = businessProcess;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((businessProcess == null) ? 0 : businessProcess.hashCode());
		result = prime * result + ((consequence == null) ? 0 : consequence.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WfAction other = (WfAction) obj;
		if (businessProcess == null) {
			if (other.businessProcess != null)
				return false;
		} else if (!businessProcess.equals(other.businessProcess))
			return false;
		if (consequence == null) {
			if (other.consequence != null)
				return false;
		} else if (!consequence.equals(other.consequence))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
