package org.ihtsdo.qa.store.model.view;

import org.ihtsdo.qa.store.model.DispositionStatus;
import org.ihtsdo.qa.store.model.QACase;
import org.ihtsdo.qa.store.model.TerminologyComponent;

public class QACasesReportLine {
	
	private QACase qaCase;
	private TerminologyComponent component;
	private DispositionStatus disposition;
	
	public QACasesReportLine(QACase qaCase, TerminologyComponent component,
			DispositionStatus disposition) {
		super();
		this.qaCase = qaCase;
		this.component = component;
		this.disposition = disposition;
	}
	
	public QACase getQaCase() {
		return qaCase;
	}
	public void setQaCase(QACase qaCase) {
		this.qaCase = qaCase;
	}
	public TerminologyComponent getComponent() {
		return component;
	}
	public void setComponent(TerminologyComponent component) {
		this.component = component;
	}
	public DispositionStatus getDisposition() {
		return disposition;
	}
	public void setDisposition(DispositionStatus disposition) {
		this.disposition = disposition;
	}
}
