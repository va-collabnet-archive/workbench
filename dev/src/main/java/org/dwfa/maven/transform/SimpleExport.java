package org.dwfa.maven.transform;

import java.io.IOException;
import java.io.Writer;

import org.dwfa.maven.I_ReadAndTransform;

public class SimpleExport extends AbstractExport {

	private String[] columns;
	
	private I_ReadAndTransform[] transformers;
	
	public void addTransformToSubclass(I_ReadAndTransform t) {
		if (transformers == null) {
			transformers = new I_ReadAndTransform[columns.length];
		}

		for (int i = 0; i < columns.length; i++) {
			if (columns[i].equals(t.getName())) {
				transformers[i] = t;
				return;
			}
		}
	}

	public void writeRec() throws IOException {
		for (int i = 0; i < transformers.length; i++) {
			if (i != 0) {
				w.append(getOutputColumnDelimiter());
			}
			w.append(transformers[i].getLastTransform());
		}
		w.append('\n');
	}

	public String[] getColumns() {
		return columns;
	}

	public void setColumns(String[] columns) {
		this.columns = columns;
	}

	public void writeColumns(Writer w) throws IOException {
		for (int i = 0; i < columns.length; i++) {
			if (i != 0) {
				w.append(getOutputColumnDelimiter());
			}
			w.append(columns[i]);
		}
		w.append('\n');
	}

	public void close() throws IOException {
		w.close();
	}

	protected void prepareForClose() {
		// Nothing to do
	}

}
