package org.ihtsdo.qadb.data.view;

import org.ihtsdo.qadb.data.DispositionStatus;
import org.ihtsdo.qadb.data.QACase;
import org.ihtsdo.qadb.data.TerminologyComponent;


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
