package org.dwfa.maven.transform;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.StringTokenizer;

import org.dwfa.maven.I_ReadAndTransform;
import org.dwfa.util.text.StringToWord;

public class LowerCaseWordIndex extends AbstractExport {

	private String descIdColumn;
	private I_ReadAndTransform descIdTransformer;
	private String conceptIdColumn;
	private I_ReadAndTransform conceptIdTransformer;
	private String[] textColumns;
	
	protected I_ReadAndTransform[] transformers;
	
	private int maxLength = 0;
	

	public void writeColumns(Writer w) throws IOException {
		w.write(descIdColumn);
		w.write(getOutputColumnDelimiter());
		w.write(conceptIdColumn);
		for (String column: textColumns) {
			w.write(getOutputColumnDelimiter());
			w.write(column);
			
		}
		w.write(WINDOWS_LINE_TERMINATOR);

	}

	public void addTransformToSubclass(I_ReadAndTransform t) {
		if (transformers == null) {
			transformers = new I_ReadAndTransform[textColumns.length];
		}

		if (conceptIdColumn.equals(t.getName())) {
			conceptIdTransformer = t;
		} else if (descIdColumn.equals(t.getName())) {
			descIdTransformer = t;
		} else for (int i = 0; i < textColumns.length; i++) {
			if (textColumns[i].equals(t.getName())) {
				transformers[i] = t;
				return;
			}
		}
	}


   public void writeRec() throws IOException {
		
		HashSet<String> words = new HashSet<String>();
		for (int i = 0; i < transformers.length; i++) {
			String stringField = transformers[i].getLastTransform();
			StringTokenizer st=new StringTokenizer(stringField, StringToWord.WORD_DELIMITERS);
//			 Split your words.
			while(st.hasMoreTokens()){
				String t=st.nextToken();
				maxLength = Math.max(maxLength, t.length());
				words.add(t.toLowerCase());
			}
		}
		
		for (Object word: words) {
			w.append(descIdTransformer.getLastTransform());
			w.append(getOutputColumnDelimiter());
			w.append(conceptIdTransformer.getLastTransform());
			w.append(getOutputColumnDelimiter());
			w.append(word.toString());
			w.append(WINDOWS_LINE_TERMINATOR);
		}

	}

	
	protected void prepareForClose() throws IOException {
		System.out.println("Max word length: " + maxLength);
	}

}
