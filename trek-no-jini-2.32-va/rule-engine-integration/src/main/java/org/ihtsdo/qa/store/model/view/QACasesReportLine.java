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
package org.ihtsdo.qa.store.model.view;

import org.ihtsdo.qa.store.model.DispositionStatus;
import org.ihtsdo.qa.store.model.QACase;
import org.ihtsdo.qa.store.model.TerminologyComponent;

/**
 * The Class QACasesReportLine.
 */
public class QACasesReportLine {

	/** The qa case. */
	private QACase qaCase;
	
	/** The component. */
	private TerminologyComponent component;
	
	/** The disposition. */
	private DispositionStatus disposition;

	/**
	 * Instantiates a new qA cases report line.
	 *
	 * @param qaCase the qa case
	 * @param component the component
	 * @param disposition the disposition
	 */
	public QACasesReportLine(QACase qaCase, TerminologyComponent component, DispositionStatus disposition) {
		super();
		this.qaCase = qaCase;
		this.component = component;
		this.disposition = disposition;
	}

	/**
	 * Gets the qa case.
	 *
	 * @return the qa case
	 */
	public QACase getQaCase() {
		return qaCase;
	}

	/**
	 * Sets the qa case.
	 *
	 * @param qaCase the new qa case
	 */
	public void setQaCase(QACase qaCase) {
		this.qaCase = qaCase;
	}

	/**
	 * Gets the component.
	 *
	 * @return the component
	 */
	public TerminologyComponent getComponent() {
		return component;
	}

	/**
	 * Sets the component.
	 *
	 * @param component the new component
	 */
	public void setComponent(TerminologyComponent component) {
		this.component = component;
	}

	/**
	 * Gets the disposition.
	 *
	 * @return the disposition
	 */
	public DispositionStatus getDisposition() {
		return disposition;
	}

	/**
	 * Sets the disposition.
	 *
	 * @param disposition the new disposition
	 */
	public void setDisposition(DispositionStatus disposition) {
		this.disposition = disposition;
	}
}
