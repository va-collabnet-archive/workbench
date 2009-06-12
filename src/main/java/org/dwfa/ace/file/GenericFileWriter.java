package org.dwfa.ace.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.dwfa.tapi.TerminologyException;

/**
 * Generic file writer that allows enables subclassing to create typed writers that write
 * out objects each on a new line of a file.
 * Subclasses may override the <code>serialize</code> method to customise the representation
 * of the objects in the file, the default is the objects <code>toString</code> method.
 * 
 * @author Dion
 *
 * @param <T>
 */
public class GenericFileWriter<T> {

	private BufferedWriter writer;
	
	/**
	 * Opens the specified file for writing.
	 * 
	 * @param outputFile file to write to
	 * @param append if true the file will be appended to, 
	 * 		if false the file will be overwritten
	 * @throws IOException
	 */
	public void open(File outputFile, boolean append) throws IOException {
		writer = new BufferedWriter(new FileWriter(outputFile, append));
	}

	/**
	 * Writes the specified list of objects to the file
	 * 
	 * @param objectList
	 * @throws IOException
	 * @throws TerminologyException
	 */
	public void write(List<T> objectList) throws IOException, TerminologyException {
		for (T object : objectList) {
			write(object);
		}
	}
	
	/**
	 * Writes the specified object to the file
	 * 
	 * @param object
	 * @throws IOException
	 * @throws TerminologyException
	 */
	public void write(T object) throws IOException, TerminologyException {
		writer.append(serialize(object));
		writer.newLine();
	}

	/**
	 * Format the object for output in the file - not that the representation
	 * cannot contain newline characters.
	 * 
	 * @param object
	 * @return the object formatted for output in the file
	 * @throws IOException
	 * @throws TerminologyException
	 */
	protected String serialize(T object) throws IOException, TerminologyException {
		return object.toString();
	}

	/**
	 * Closes the writer once writing is complete
	 * @throws IOException
	 */
	public void close() throws IOException {
		writer.close();
		writer = null;
	}
	
}
