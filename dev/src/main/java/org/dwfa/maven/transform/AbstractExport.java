package org.dwfa.maven.transform;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.maven.I_ReadAndTransform;
import org.dwfa.maven.I_TransformAndWrite;
import org.dwfa.maven.InputFileSpec;
import org.dwfa.maven.Transform;

public abstract class AbstractExport implements I_TransformAndWrite {

	private Character outputColumnDelimiter = '\t';
	private Character outputCharacterDelimiter = '"';
	private String outputEncoding = "UTF-8";
	private boolean writeHeaders = false;
	private boolean exportOnlyActive = true;
	protected Writer w;
	private I_ReadAndTransform statusTransform;
	private String statusTransformName;
	private Map uuidToNativeMap;
	private Set activeSet = new HashSet();

	/**
	 * @parameter
	 */
	private boolean append = false;

	public Character getOutputCharacterDelimiter() {
		return outputCharacterDelimiter;
	}

	public void setOutputCharacterDelimiter(Character outputCharacterDelimiter) {
		this.outputCharacterDelimiter = outputCharacterDelimiter;
	}

	public Character getOutputColumnDelimiter() {
		return outputColumnDelimiter;
	}

	public void setOutputColumnDelimiter(Character outputColumnDelimiter) {
		this.outputColumnDelimiter = outputColumnDelimiter;
	}

	public String getOutputEncoding() {
		return outputEncoding;
	}

	public void setOutputEncoding(String outputEncoding) {
		this.outputEncoding = outputEncoding;
	}

	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append(this.getClass().getSimpleName());
		b.append(": \n");
		b.append("\nColumn delimiter: ");
		if (Character.isWhitespace(outputColumnDelimiter)) {
			b.append(InputFileSpec.whiteSpaceCharToUnicode(outputColumnDelimiter));
		} else {
			b.append(outputColumnDelimiter);
		}
		b.append("\nCharacter delimiter: ");
		if (Character.isWhitespace(outputCharacterDelimiter)) {
			b.append(InputFileSpec.whiteSpaceCharToUnicode(outputCharacterDelimiter));
		} else {
			b.append(outputCharacterDelimiter);
		}
		b.append("\nEncoding: ");
		b.append(outputEncoding);
		return b.toString();
	}

	private String fileName;

	public AbstractExport() {
		super();
	}

	public final void processRec() throws IOException {
		if (exportOnlyActive == false) {
			writeRec();
		} else {
			if (statusTransform != null) {
                try {
    				if (activeSet.contains(Integer.parseInt(statusTransform.getLastTransform()))) {
    					writeRec();
    				}
                } catch (NumberFormatException e) {
                    //System.out.println("Number format exception in "
                    //        + "AbstractExport parsing: "
                     //       + statusTransform.getLastTransform());
                    // ??
					writeRec();
				}
			}
		}
	}

	public abstract void writeRec() throws IOException;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void init(Writer w, Transform t) throws Exception {
		this.w = w;
		if (append == false && writeHeaders == true) {
			writeColumns(w);
		}
		if (exportOnlyActive) {

			activeSet.add(t.uuidToNid(ArchitectonicAuxiliary.Concept.ACTIVE.getUids()));
			activeSet.add(t.uuidToNid(ArchitectonicAuxiliary.Concept.CURRENT.getUids()));
			activeSet.add(t.uuidToNid(ArchitectonicAuxiliary.Concept.LIMITED.getUids()));
			activeSet.add(t.uuidToNid(ArchitectonicAuxiliary.Concept.PENDING_MOVE.getUids()));
			System.out.println("ACTIVE: " + ArchitectonicAuxiliary.Concept.ACTIVE.getUids());
			System.out.println("CURRENT: " + ArchitectonicAuxiliary.Concept.CURRENT.getUids());
			System.out.println("LIMITED: " + ArchitectonicAuxiliary.Concept.LIMITED.getUids());
			System.out.println("export only active: " + activeSet);
		}
	}
	protected abstract void writeColumns(Writer w) throws IOException;

	public final void addTransform(I_ReadAndTransform t) {
		if (exportOnlyActive) {
			if (statusTransformName.equals(t.getName())) {
				statusTransform = t;
			}
		}
		addTransformToSubclass(t);
	}

	protected abstract void addTransformToSubclass(I_ReadAndTransform t);

	public void close() throws IOException {
		prepareForClose();
		w.close();
	}
	protected abstract void prepareForClose() throws IOException;

	public boolean isAppend() {
		return append;
	}
	public boolean append() {
		return append;
	}

	public void setAppend(boolean append) {
		this.append = append;
	}

	public boolean writeHeaders() {
		return writeHeaders;
	}
	public boolean isWriteHeaders() {
		return writeHeaders;
	}

	public void setWriteHeaders(boolean writeHeaders) {
		this.writeHeaders = writeHeaders;
	}

	public boolean exportOnlyActive() {
		return exportOnlyActive;
	}
	public boolean isExportOnlyActive() {
		return exportOnlyActive;
	}

	public void setExportOnlyActive(boolean exportOnlyActive) {
		this.exportOnlyActive = exportOnlyActive;
	}

	protected I_ReadAndTransform getStatusTransform() {
		return statusTransform;
	}

	protected void setStatusTransform(I_ReadAndTransform statusTransform) {
		this.statusTransform = statusTransform;
	}

	public String getStatusTransformName() {
		return statusTransformName;
	}

	public void setStatusTransformName(String statusTransformName) {
		this.statusTransformName = statusTransformName;
	}

}