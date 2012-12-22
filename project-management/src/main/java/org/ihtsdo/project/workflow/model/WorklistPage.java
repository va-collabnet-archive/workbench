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

import java.io.Serializable;

/**
 * The Class WorklistPage.
 */
public class WorklistPage implements Serializable{
	
	/** The start line. */
	private Integer startLine;
	
	/** The page lenght. */
	private Integer pageLenght;

	/**
	 * Instantiates a new worklist page.
	 */
	public WorklistPage() {
		super();
	}

	/**
	 * Instantiates a new worklist page.
	 *
	 * @param startLine the start line
	 * @param pageLenght the page lenght
	 */
	public WorklistPage(Integer startLine, Integer pageLenght) {
		super();
		this.startLine = startLine;
		this.pageLenght = pageLenght;
	}

	/**
	 * Gets the start line.
	 *
	 * @return the start line
	 */
	public Integer getStartLine() {
		return startLine;
	}

	/**
	 * Sets the start line.
	 *
	 * @param startLine the new start line
	 */
	public void setStartLine(Integer startLine) {
		this.startLine = startLine;
	}

	/**
	 * Gets the page lenght.
	 *
	 * @return the page lenght
	 */
	public Integer getPageLenght() {
		return pageLenght;
	}

	/**
	 * Sets the page lenght.
	 *
	 * @param pageLenght the new page lenght
	 */
	public void setPageLenght(Integer pageLenght) {
		this.pageLenght = pageLenght;
	}

}
